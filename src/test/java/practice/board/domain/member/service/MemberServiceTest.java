package practice.board.domain.member.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Address;
import practice.board.domain.Member;
import practice.board.repository.MemberRepository;
import practice.board.service.MemberService;
import practice.board.web.dto.member.MemberResDto;
import practice.board.web.dto.member.MemberSaveReqDto;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired EntityManager em;
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;


    @Test
    void save_success() {
        //given
        Member member = Member.createMember("username", "passP1234!", "email@test.com", "nick", 20, null);
        MemberSaveReqDto dto = MemberSaveReqDto.toDto(member);

        //when
        Long savedId = memberService.saveMember(dto);
        Member savedMember = memberRepository.findById(savedId).get();

        //then
        assertThat(savedMember).isEqualTo(member);
    }

    @DisplayName("email 중복이면 가입 불가")
    @Test
    void save_fail_duplicateEmail() {
        //TODO memberTest 의 코드랑 중복이네,,, 이런 회원가입시 제약조건은 service 단에서 체크해야 하겠구나. 여기서 테스트하니까 em.flush() 이런거 사용할 필요가 없네
        //given
        Member member1 = Member.createMember("username", "Password1234!", "email@email.com", "nick", null, null);
        MemberSaveReqDto dto1 = MemberSaveReqDto.toDto(member1);
        memberService.saveMember(dto1);

        Member member2 = Member.createMember("username2", "Password1234!", "email2@email.com", "nick", null, null);
        MemberSaveReqDto dto2 = MemberSaveReqDto.toDto(member2);

        //when, then
        assertThatThrownBy(() -> memberService.saveMember(dto2))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void update_success() {
        //given
        String password = "dks!12DKw";
        Member member = Member.createMember("username", password, "email@email.com", "nick", null, null);
        Long memberId = memberService.saveMember(MemberSaveReqDto.toDto(member));

        //when
        String newPassword = "dksh3235!!";
        String newNickname = "newNickname";
        Integer newAge = 20;
        Address newAddress = new Address("city", "street", "11111");
        memberService.update(memberId, password, newPassword, newNickname, newAge, newAddress);

        //then
        assertThat(member.getNickname()).isEqualTo(newNickname);
        assertThat(member.getAge()).isEqualTo(newAge);
        assertThat(member.getAddress()).isEqualTo(newAddress);
    }

    @DisplayName("checkPassword 가 일치하지 않아서 예외 발생")
    @Test
    void update_exception_invalidCheckPassword() {
        //given
        String password = "dks!12DKw";
        Member member = Member.createMember("username", password, "email@email.com", "nick", null, null);
        Long memberId = memberService.saveMember(MemberSaveReqDto.toDto(member));

        //when, then
        String newPassword = "abwk345!.df";

        assertThatThrownBy(() ->
                memberService.update(memberId, password+1, newPassword, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("newPassword 형식이 맞지 않아서 예외 발생")
    @Test
    void update_exception_invalid_newPassword() {
        //given
        String password = "dks!12DKw";
        Member member = Member.createMember("username", password, "email@email.com", "nick", null, null);
        Long memberId = memberService.saveMember(MemberSaveReqDto.toDto(member));

        //when, then
        String newPassword = "1234";

        assertThatThrownBy(() -> memberService.update(memberId, password, newPassword, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @DisplayName("update의 인수로 null 입력하면 수정되지 않음")
    @Test
    void update_null() {
        //given
        String password = "dks!12DKw";
        Member member = Member.createMember("username", password, "email@email.com", "nick", null, null);
        Long memberId = memberService.saveMember(MemberSaveReqDto.toDto(member));

        //when
        String newNickname = "nickname";
        memberService.update(memberId, password, null, newNickname, null, null);

        //then
        assertThat(member.validatePassword(passwordEncoder, password)).isTrue();  //기존과 동일한 password
        assertThat(member.getNickname()).isEqualTo("nickname");  //기존과 동일한 닉네임일 경우 변하지 않고 그대로
        assertThat(member.getAge()).isEqualTo(20);  //변하지 않고 그대로
        assertThat(member.getAddress()).isNull();  //변하지 않고 그대로 (기존 address 는 null)
    }


    @Test
    void delete() {
        //given
        


    }

    @DisplayName("Member -> MemberResDto 변환")
    @Test
    void convert() {
        Member member = Member.createMember("test", "Password1234!", "test@email.com", "nick", null, null);
        MemberSaveReqDto dto = MemberSaveReqDto.toDto(member);
        Long savedId = memberService.saveMember(dto);
        memberService.toMemberResDto(savedId);
    }


}