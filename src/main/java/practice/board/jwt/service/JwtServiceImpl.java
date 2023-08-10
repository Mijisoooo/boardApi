package practice.board.jwt.service;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.board.repository.MemberRepository;
import practice.board.service.LoginService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Transactional
@Service
@RequiredArgsConstructor
@Setter(value = AccessLevel.PRIVATE)
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessTokenValidityInSeconds;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidityInSeconds;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;


    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USERNAME_CLAIM = "username";
    private static final String BEARER = "Bearer ";


    private final MemberRepository memberRepository;
    private final LoginService loginService;



    @Override
    public String createAccessToken(String username) {
        return Jwts.builder()  //JWT 토큰을 생성하는 빌더를 반환
                .setSubject(ACCESS_TOKEN_SUBJECT)
                .setExpiration(new Date((System.currentTimeMillis() + accessTokenValidityInSeconds * 1000)))  //현재시간 + 80초
                .claim(USERNAME_CLAIM, username)  //claim으로는 username만 사용. 추가 가능
                .signWith(SignatureAlgorithm.HS512, secret)  //HMAC512 알고리즘 사용하며 secret 키로 암호화
                .compact();
    }

    @Override
    public String createRefreshToken() {
        return Jwts.builder()
                .setSubject(REFRESH_TOKEN_SUBJECT)
                .setExpiration(new Date((System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000)))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    @Override
    public void updateRefreshToken(String username, String refreshToken) {
        memberRepository.findByUsername(username)
                .ifPresentOrElse(
                        member -> member.updateRefreshToken(refreshToken),
                        () -> new Exception("회원이 없습니다. username:" + username)
                );
    }

    @Override
    public void destroyRefreshToken(String username) {
        memberRepository.findByUsername(username)
                .ifPresentOrElse(
                        member -> member.destroyRefreshToken(),
                        () -> new Exception("회원이 없습니다. username:" + username)
                );
    }

    @Override
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
        setRefreshTokenHeader(response, refreshToken);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(ACCESS_TOKEN_SUBJECT, accessToken);
        tokenMap.put(REFRESH_TOKEN_SUBJECT, refreshToken);
    }

    @Override
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(ACCESS_TOKEN_SUBJECT, accessToken);
    }

    @Override
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    @Override
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    @Override
    public String extractUsername(String accessToken) {
        String token = accessToken.replace(BEARER, "");
        return (String) Jwts
                .parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(USERNAME_CLAIM);

    }

    /**
     * jwt 토큰에서 인증정보 조회
     */
    @Override
    public Authentication getAuthentication(String accessToken) {

        String username = extractUsername(accessToken);
        UserDetails userDetails = loginService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()); //credentials 비워둠!
    }


    @Override
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, BEARER + accessToken);
    }

    @Override
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader, BEARER + refreshToken);
    }

    /**
     * 토큰의 유효성, 만료여부 체크
     */
    @Override
    public boolean isValid(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {       //TODO 예외 처리 어떻게 하면 좋을까??
            log.error("ExpiredJwtException", e);
            return false;
        } catch(MalformedJwtException e) {
            log.error("MalformedJwtException", e);
            return false;
        } catch(UnsupportedJwtException e) {
            log.error("UnsupportedJwtException", e);
            return false;
        } catch(SignatureException e) {
            log.error("SignatureException", e);
            return false;
        } catch (Exception e) {
            log.error("invalid JWT", e);
            return false;
        }
    }
}
