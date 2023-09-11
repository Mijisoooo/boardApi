package practice.board.config.guard;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.MemberRepository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
class LoginMemberArgumentResolverTest {

    @Autowired LoginMemberArgumentResolver loginMemberArgumentResolver;
    @Autowired MemberRepository memberRepository;

    private final String USERNAME = "testUser";
    private final String PASSWORD = "Pass1255!";

    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD)
    void 정상_동작() {
        Member member = Member.createMember(USERNAME, PASSWORD, "email@email.com", "nicky23", null, null);
        memberRepository.save(member);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member foundMember = memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        assertThat(foundMember).isNotNull();
    }


}