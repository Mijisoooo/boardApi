package practice.board.web.controller.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.MemberRepository;
import practice.board.service.MemberService;
import practice.board.web.dto.member.MemberLoginReqDto;
import practice.board.web.dto.member.MemberSaveReqDto;


import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class MemberApiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberService memberService;

    private final String username = "test";
    private final String password = "Aaksj123dg!";
    private final String invalidPassword = "1234";
    private final String email = "test@email.com";
    private final String nickname = "testUser";
    private int age = 20;


    private String getSignupData(String username, String password, String email, String nickname, int age) throws JsonProcessingException {

        MemberSaveReqDto dto = MemberSaveReqDto.builder()
                .username(username)
                .password(password)
                .email(email)
                .nickname(nickname)
                .age(age)
                .build();

        return objectMapper.writeValueAsString(dto);
    }

    private ResultActions signUp(String signupData) throws Exception {
        return mockMvc.perform(post("/api/members")
                        .content(signupData)
                        .contentType(MediaType.APPLICATION_JSON));

    }



    @DisplayName("회원가입 성공")
    @Test
    void signup_success() throws Exception {

        //given
        String signupData = getSignupData(username, password, email, nickname, age);

        //when
        ResultActions actions = signUp(signupData);

        //then
        actions.andExpect(status().isCreated())
                .andDo(print());

        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(memberRepository.findAll().size()).isEqualTo(1);

    }

    @DisplayName("회원가입 실패 - 비밀번호 형식 안맞음")
    @Test
    void signup_fail_invalid_password() throws Exception {

        //given
        String signupData = getSignupData(username, invalidPassword, email, nickname, age);

        //when
        ResultActions actions = signUp(signupData);

        //then
        actions.andExpect(status().isBadRequest())
                .andDo(print());

        assertThatThrownBy(() -> memberRepository.findByUsername(username).orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND)))
                .isInstanceOf(ApiException.class);
        assertThat(memberRepository.findAll().size()).isEqualTo(0);

    }

    @DisplayName("회원가입 실패 - email 형식 안맞음")
    @Test
    void signup_fail_invalid_email() throws Exception {
        //given
        String signupData = getSignupData(username, password, "invalidEmailFormat", nickname, age);

        //when
        ResultActions actions = signUp(signupData);

        //then
        actions.andExpect(status().isBadRequest())
                .andDo(print());

        assertThatThrownBy(() -> memberRepository.findByUsername(username).orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND)))
                .isInstanceOf(ApiException.class);
        assertThat(memberRepository.findAll().size()).isEqualTo(0);
    }

    @DisplayName("회원가입 실패 - nickname null")
    @Test
    void signup_fail_nickname_null() throws Exception {
        //given
        String signupData = getSignupData(username, password, email, null, age);

        //when
        ResultActions actions = signUp(signupData);

        //then
        actions.andExpect(status().isBadRequest())
                .andDo(print());

        assertThatThrownBy(() -> memberRepository.findByUsername(username).orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND)))
                .isInstanceOf(ApiException.class);
        assertThat(memberRepository.findAll().size()).isEqualTo(0);
    }


    @DisplayName("개별 회원 조회")
    @Test
    void find_member_success() throws Exception {

        //given
        String signupData = getSignupData(username, password, email, nickname, age);
        signUp(signupData);

        //when
        Member member = memberRepository.findByUsername(username).get();
        Long id = member.getId();

        ResultActions actions = mockMvc.perform(get("/api/member/{id}", id));

        //then
        actions.andExpect(status().isOk())
                .andDo(print());

    }

    @DisplayName("로그인 성공")
    @Test
    void login_success() throws Exception {
        //given
        String signupData = getSignupData(username, password, email, nickname, age);
        signUp(signupData);

        MemberLoginReqDto loginDto = MemberLoginReqDto.builder()
                .username(username)
                .password(password)
                .build();

        String loginData = objectMapper.writeValueAsString(loginDto);
        Member member = memberRepository.findByUsername(username).get();

        //when
        ResultActions actions = mockMvc.perform(post("/api/login")
                .content(loginData)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        actions.andExpect(status().isOk()).andDo(print());
        assertThat(member.getRefreshToken()).isNotNull();
    }

    @DisplayName("로그인 실패 - wrong username")
    @Test
    void login_fail_wrong_username() throws Exception {
        //given
        String signupData = getSignupData(username, password, email, nickname, age);
        signUp(signupData);

        MemberLoginReqDto loginDto = MemberLoginReqDto.builder()
                .username(username+1)
                .password(password)
                .build();

        String loginData = objectMapper.writeValueAsString(loginDto);
        Member member = memberRepository.findByUsername(username).get();

        //when
        ResultActions actions = mockMvc.perform(post("/api/login")
                .content(loginData)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        actions.andExpect(status().isUnauthorized()).andDo(print());
        assertThat(member.getRefreshToken()).isNull();

    }

    @DisplayName("로그인 실패 - wrong password")
    @Test
    void login_fail_wrong_password() throws Exception {
        //given
        String signupData = getSignupData(username, password, email, nickname, age);
        signUp(signupData);

        MemberLoginReqDto loginDto = MemberLoginReqDto.builder()
                .username(username)
                .password(password+1)
                .build();

        String loginData = objectMapper.writeValueAsString(loginDto);
        Member member = memberRepository.findByUsername(username).get();

        //when
        ResultActions actions = mockMvc.perform(post("/api/login")
                .content(loginData)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        actions.andExpect(status().isUnauthorized()).andDo(print());
        assertThat(member.getRefreshToken()).isNull();
    }


}