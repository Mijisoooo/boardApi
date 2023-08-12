package practice.board.global.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;
import practice.board.domain.Role;
import practice.board.jwt.JwtService;
import practice.board.repository.MemberRepository;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Transactional
class JwtServiceTest {

    @Autowired MemberRepository memberRepository;
    @Autowired JwtService jwtService;
    @Autowired EntityManager em;


    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USERNAME_CLAIM = "username";
    private static final String BEARER = "Bearer ";
    private static final String USERNAME = "username";


    @BeforeEach
    void init() {
        Member member = Member.builder()
                .username(USERNAME)
                .password("1234")
                .email("test.com")
                .nickname("nick")
                .role(Role.USER)
                .build();

        memberRepository.save(member);
        clear();
    }

    void clear() {
        em.flush();
        em.clear();
    }



    //    로그인 실패시 JWT 생성안됨
    //    로그인 성공 후 다시 request 보내면 인증 만료 안된 경우 accessToken 유효한 것 확인


    @Test
    void createAccessToken_success() {

        //given
        String accessToken = jwtService.createAccessToken(USERNAME);

        //when
        Claims verify = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(accessToken).getBody();


        //then
        assertThat(verify.get(USERNAME_CLAIM).toString()).isEqualTo(USERNAME);
        assertThat(verify.getSubject()).isEqualTo(ACCESS_TOKEN_SUBJECT);

    }

    @Test
    void createRefreshToken() {
        //given
        String refreshToken = jwtService.createRefreshToken();

        //when
        Claims verify = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(refreshToken).getBody();

        //then
        assertThat(verify.get(USERNAME_CLAIM).toString()).isNull();
        assertThat(verify.getSubject()).isEqualTo(REFRESH_TOKEN_SUBJECT);

    }

    @Test
    void updateRefreshToken() throws InterruptedException {
        //given
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(USERNAME, refreshToken);
        Claims verify1 = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(refreshToken).getBody();
        clear();
        Thread.sleep(3000);

        //when
        String updatedRefreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(USERNAME, updatedRefreshToken);
        Claims verify2 = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(updatedRefreshToken).getBody();
        clear();

        //then
        assertThrows(Exception.class, () -> memberRepository.findByRefreshToken(refreshToken).get());
        assertThat(memberRepository.findByRefreshToken(updatedRefreshToken).get().getUsername()).isEqualTo(USERNAME);
        assertThat(verify1.getExpiration()).isBefore(verify2.getExpiration());
    }

    @Test
    void destroyRefreshToken() {
        //given
        String refreshToken = jwtService.createRefreshToken();
        jwtService.updateRefreshToken(USERNAME, refreshToken);
        clear();

        //when
        jwtService.destroyRefreshToken(USERNAME);

        //then
        assertThrows(Exception.class, () -> memberRepository.findByRefreshToken(refreshToken).get());

        Member member = memberRepository.findByUsername(USERNAME).get();
        assertThat(member.getRefreshToken()).isNull();

    }

    @Test
    void sendToken() {
        //given
        MockHttpServletResponse response = new MockHttpServletResponse();
        String accessToken = jwtService.createAccessToken(USERNAME);
        String refreshToken = jwtService.createRefreshToken();

        //when
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        //then
        assertThat(response.getHeader(accessHeader)).isEqualTo(accessToken);
        assertThat(response.getHeader(refreshHeader)).isEqualTo(refreshToken);

    }

    private HttpServletRequest setToken(String accessToken, String refreshToken) {

        MockHttpServletResponse response = new MockHttpServletResponse();
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        String headerAccessToken = response.getHeader(accessHeader);
        String headerRefreshToken = response.getHeader(refreshHeader);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(accessHeader, BEARER + headerAccessToken);
        request.addHeader(refreshHeader, BEARER + headerRefreshToken);

        return request;
    }

    @Test
    void extractAccessToken() {
        //given
        String accessToken = jwtService.createAccessToken(USERNAME);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest request = setToken(accessToken, refreshToken);

        //when
        String extractedAccessToken = jwtService.extractAccessToken(request).get();

        //then
        assertThat(extractedAccessToken).isEqualTo(accessToken);
    }

    @Test
    void extractRefreshToken() {
        //given
        String accessToken = jwtService.createAccessToken(USERNAME);
        String refreshToken = jwtService.createRefreshToken();
        HttpServletRequest request = setToken(accessToken, refreshToken);

        //when
        String extractedRefreshToken = jwtService.extractRefreshToken(request).get();

        //then
        assertThat(extractedRefreshToken).isEqualTo(refreshToken);
    }

    @Test
    void extractUsername() {
        //given
        String accessToken = jwtService.createAccessToken(USERNAME);

        //when
        String extractedUsername = jwtService.extractUsername(accessToken);

        //then
        assertThat(extractedUsername).isEqualTo(USERNAME);
    }



}