package practice.board.global.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;
import practice.board.jwt.JwtService;
import practice.board.repository.MemberRepository;
import practice.board.service.MemberService;
import practice.board.web.dto.member.MemberSaveReqDto;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Slf4j
class JwtAuthenticationFilterTest {

    @Autowired EntityManager em;
    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberService memberService;
    @Autowired PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static String KEY_USERNAME = "username";
    private static String KEY_PASSWORD = "password";
    private static String USERNAME = "username";
    private static String PASSWORD = "123456789";

    private static String LOGIN_URL = "/login";

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String BEARER = "Bearer ";

    private ObjectMapper objectMapper = new ObjectMapper();


    private void clear(){
        em.flush();
        em.clear();
    }

    @BeforeEach
    void init() {
        Member member = Member.createMember(USERNAME, "password1234!", "test@email.com", "nick", 20, null);
        memberService.saveMember(MemberSaveReqDto.toDto(member));
        clear();
    }

    private Map getUsernamePasswordMap(String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_USERNAME, username);
        map.put(KEY_PASSWORD, password);
        return map;
    }

    private Map<String, String> getAccessAndRefreshToken() throws Exception {
        getUsernamePasswordMap(USERNAME, PASSWORD);
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getUsernamePasswordMap(USERNAME, PASSWORD))))
                .andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader).replace(BEARER, "");
        String refreshToken = result.getResponse().getHeader(refreshHeader).replace(BEARER, "");

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put(accessHeader, accessToken);
        tokenMap.put(refreshHeader, refreshToken);

        return tokenMap;
    }


    /**
     * access token : 유효
     * refresh token : 없음
     */
    @Test
    void 유효한_at_200() throws Exception {
        Map<String, String> map = getAccessAndRefreshToken();
        String accessToken = map.get(accessHeader);

        mockMvc.perform(get("/api/test")
                .header(accessHeader, BEARER + accessToken))
                .andExpectAll(status().isOk());

    }

    /**
     * access token : 유효 X
     * refresh token : 없음
     */
    @Test
    void 유효X_at만_보냄_401() throws Exception {
        //given
        Map<String, String> map = getAccessAndRefreshToken();
        String accessToken = map.get(accessHeader);

        //when,then
        mockMvc.perform(get("/api/test")
                .header(accessHeader, BEARER + accessToken + "1"))
                .andExpectAll(status().isUnauthorized());
    }

    /**
     * access token : 없음
     * refresh token : 유효
     */
    @Test
    void 유효한_rt_보내서_at_재발급_200() throws Exception {
        //given
        Map<String, String> map = getAccessAndRefreshToken();
        String refreshToken = map.get(refreshHeader);

        //when, then
        MvcResult result = mockMvc.perform(get(LOGIN_URL + "123")
                        .header(refreshHeader, BEARER + refreshToken))
                .andExpect(status().isNotFound()).andReturn();

        String accessToken = result.getResponse().getHeader(accessHeader);

        String username = jwtService.extractUsername(accessToken);
        Assertions.assertThat(username).isEqualTo(USERNAME);

    }


    /**
     * access token : 없음
     * refresh token : 없음
     */
    @Test
    void AT없음_RT없음() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/test"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

    }





}