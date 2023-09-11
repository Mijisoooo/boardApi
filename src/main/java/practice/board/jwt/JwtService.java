package practice.board.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.MemberRepository;
import practice.board.service.CustomUserDetailsService;

import java.util.*;
import java.util.stream.Collectors;

import static practice.board.exception.ErrorCode.*;

@Transactional
@Service
@RequiredArgsConstructor
@Setter(value = AccessLevel.PRIVATE)
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String key;

    @Value("${jwt.access.expiration}")
    private long accessTokenValidityInSeconds;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidityInSeconds;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;


    private static final String TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_VALUE = "refresh";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORITIES_KEY = "auth";


    private final MemberRepository memberRepository;
    private final CustomUserDetailsService customUserDetailsService;


    public String createAccessToken(String username) {

        Date exp = new Date(System.currentTimeMillis() + accessTokenValidityInSeconds * 1000);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        //access token 생성해서 리턴
        return Jwts.builder()
                .setSubject(userDetails.getUsername())  //payload "sub": "username"
                .claim(AUTHORITIES_KEY, userDetails.getAuthorities().toString())  //payload "auth": "USER"
                .setExpiration(exp)  //payload "exp": 1416239232 (예시)
                .signWith(SignatureAlgorithm.HS512, key)  //header "alg": "HS512" (HMAC512 알고리즘 사용하며 secret 키로 암호화)
                .compact();
    }

    public String createRefreshToken() {

        Date exp = new Date(System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setExpiration(exp)
                .claim(TOKEN_TYPE, TOKEN_TYPE_VALUE)  //payload "token_type": "refresh"
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    public void updateRefreshToken(String username, String refreshToken) {
        memberRepository.findByUsername(username)
                .ifPresentOrElse(
                        member -> member.updateRefreshToken(refreshToken),
                        () -> new ApiException(MEMBER_NOT_FOUND, "회원이 없습니다. username:" + username)
                );
    }

    public void destroyRefreshToken(String username) {
        memberRepository.findByUsername(username)
                .ifPresentOrElse(
                        Member::destroyRefreshToken,
                        () -> new ApiException(MEMBER_NOT_FOUND, "회원이 없습니다. username:" + username)
                );
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(accessHeader, BEARER_PREFIX + accessToken);
        response.setHeader(refreshHeader, BEARER_PREFIX + refreshToken);
    }

    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(accessHeader, BEARER_PREFIX + accessToken);
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER_PREFIX))
                .map(accessToken -> accessToken.replace(BEARER_PREFIX, ""));
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER_PREFIX))
                .map(refreshToken -> refreshToken.replace(BEARER_PREFIX, ""));
    }

    public String extractUsername(String accessToken) {  //subject 로 username 넣었기에
        return parseClaims(accessToken).getSubject();
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch(ExpiredJwtException e) {  //TODO 흐으음..... 다른 방식이 좋을 것 같은데
            return e.getClaims();
        }
    }


    /**
     * SecurityContext 에 Authentication 객체 저장
     */
    public void saveAuthentication(String accessToken) {
        Authentication authentication = getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("authentication 생성 후 SecurityContext에 저장");
    }

    /**
     * jwt 토큰에서 인증정보 조회해서 Authentication 객체 생성
     */
    public Authentication getAuthentication(String accessToken) {

        //토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

/*
        //claim 에서 권한 정보 가져오기
        List<SimpleGrantedAuthority> authorities = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        //UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);
*/

        String username = extractUsername(accessToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()); //credentials 비워둠!
    }

    /**
     * 토큰의 유효성 체크
     */
    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT", e);
        } catch(MalformedJwtException e) {
            log.error("잘못된 구조를 가진 JWT", e);
        } catch(UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT", e);
        } catch(SignatureException e) {
            log.error("잘못된 JWT 서명", e);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 JWT", e);
        }
        return false;
    }
}

