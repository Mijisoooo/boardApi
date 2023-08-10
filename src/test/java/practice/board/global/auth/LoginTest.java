package practice.board.global.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;
import practice.board.domain.Role;
import practice.board.repository.MemberRepository;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

    @Autowired MockMvc mockMvc;
    @Autowired EntityManager em;
    @Autowired MemberRepository memberRepository;

    PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    ObjectMapper objectMapper = new ObjectMapper();


    private static String KEY_USERNAME = "username";
    private static String KEY_PASSWORD = "password";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "1234";
    public static String LOGIN_URL = "/login";



    private void clear() {
        em.flush();
        em.clear();
    }

    @BeforeEach
    void init() {
        Member member = Member.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .nickname("nickname")
                .email("email.com")
                .role(Role.USER)
                .age(20)
                .build();
        memberRepository.save(member);
        clear();
    }

    private Map getUsernamePasswordMap(String username, String password) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_USERNAME, username);
        map.put(KEY_PASSWORD, password);
        return map;
    }



//    private ResultActions perform(String url, MediaType mediaType, Map usernamePasswordMap) throws Exception {
//        return mockMvc.perform(MockMvcRequestBuilders
//                .post(url)
//                .contentType(mediaType)
//                .content(objectMapper.writeValueAsString(usernamePasswordMap)));
//
//    }

    private ResultActions perform(String url, MediaType mediaType, Map map) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                .post(url)
                .contentType(mediaType)
                .content(objectMapper.writeValueAsString(map)));
    }


    /**
     * 로그인 성공 -> 200 + 성공 메세지 반환
     */
    @Test
    void login_success() throws Exception {

        perform(LOGIN_URL, APPLICATION_JSON, getUsernamePasswordMap(USERNAME, PASSWORD))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

    }

    /**
     * 로그인 실패_아이디 다름 -> 200 + 실패 메세지 반환
     */
    @Test
    void login_fail_username_X() throws Exception {

        perform(LOGIN_URL, APPLICATION_JSON, getUsernamePasswordMap(USERNAME+"123", PASSWORD))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }


    /**
     * 로그인 실패 : 패스워드 다름 -> 200 + 실패 메세지 반환
     */
    @Test
    void login_fail_password_X() throws Exception {

        perform(LOGIN_URL, APPLICATION_JSON, getUsernamePasswordMap(USERNAME, PASSWORD+"123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

    }


    /**
     * 로그인 url 아니면 -> 403 forbidden
     */
    @Test
    void login_fail_url_X() throws Exception {

        perform(LOGIN_URL+"123", APPLICATION_JSON, getUsernamePasswordMap(USERNAME, PASSWORD))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    /**
     * CONTENT-TYPE != JSON -> 200 + 실패 메세지
     */
    @Test
    void login_fail_contentType_X() throws Exception {
        perform(LOGIN_URL, APPLICATION_FORM_URLENCODED, getUsernamePasswordMap(USERNAME, PASSWORD))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }


    /**
     * httpMethod != POST -> 404 notFound
     */
    @Test
    void login_fail_httpMethod_X() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                .get(LOGIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(getUsernamePasswordMap(USERNAME, PASSWORD))))
                .andDo(print())
                .andExpect(status().isNotFound());

    }




}
