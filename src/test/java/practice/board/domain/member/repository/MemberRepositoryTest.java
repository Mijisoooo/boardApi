package practice.board.domain.member.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;
import practice.board.repository.MemberRepository;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired EntityManager em;
    @Autowired
    MemberRepository memberRepository;


    @AfterEach
    private void after() {
        em.clear();
    }


    private static Member create(String username, String email, String nickname) {
        Member member = Member.createMember(username, "password123!", email, nickname, 20, null);
        member.addUserRole();
        return member;
    }


    @Test
    void 회원저장_성공() {
        //given
        Member member = create("username", "email@email.com", "nick");

        //when
        Long savedId = memberRepository.save(member);
        Member savedMember = memberRepository.findById(savedId).orElseThrow(() ->
                new NoSuchElementException("해당 회원이 없습니다. memberId=" + savedId));

        //then
        assertThat(member).isEqualTo(savedMember);

    }

    @Test
    void 회원가입시_생성시간() {
        //given
        Member member = create("username", "email@email.com", "nick");

        //when
        memberRepository.save(member);

        //then
        Member savedMember = memberRepository.findById(member.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 회원이 없습니다. memberId=" + member.getId()));
        assertThat(savedMember.getCreatedAt()).isNotNull();

    }

    @Test
    void 회원저장_아이디X_오류() {
        //given
        Member member = create(null, "email@email.com", "nick");

        //when, then
        memberRepository.save(member);
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생
    }


    @Test
    void 회원저장_닉네임X_오류() {
        //given
        Member member = create("username", "email@email.com", null);

        //when, then
        memberRepository.save(member);
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생
    }

    @Test
    void 회원저장_이메일X_오류() {
        //given
        Member member = create("username", null, "nickname");

        //when, then
        memberRepository.save(member);
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생

    }

    @Test
    void 회원저장_비밀번호X_오류() {
        //given
        Member member = Member.createMember("username", null, "email@email.com", "nick", 20, null);
        member.addUserRole();

        //when, then
        memberRepository.save(member);
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생

    }

    @Test
    void 회원저장_중복아이디_오류() {
        //given
        Member member1 = create("username", "email1@email.com", "nick1");
        Member member2 = create("username", "email2@email.com", "nick2");

        //when, then
        memberRepository.save(member1);
        memberRepository.save(member2);

        assertThrows(Exception.class, () -> em.flush());  //org.hibernate.exception.ConstraintViolationException
//        assertThatThrownBy(() -> memberRepository.save(member2))
//                .isInstanceOf(ConstraintViolationException.class);

    }

    @Test
    void 회원저장_중복이메일_오류() {
        //given
        Member member1 = create("username1", "email@email.com", "nick1");
        Member member2 = create("username2", "email@email.com", "nick2");

        //when, then
        memberRepository.save(member1);
        memberRepository.save(member2);

        assertThrows(Exception.class, () -> em.flush());  //org.hibernate.exception.ConstraintViolationException
//        assertThatThrownBy(() -> memberRepository.save(member2))
//                .isInstanceOf(ConstraintViolationException.class);

    }

    @Test
    void 회원저장_중복닉네임_오류() {
        //given
        Member member1 = create("username1", "email1@email.com", "nick");
        Member member2 = create("username2", "email2@email.com", "nick");

        //when, then
        memberRepository.save(member1);
        memberRepository.save(member2);

        assertThrows(Exception.class, () -> em.flush());  //org.hibernate.exception.ConstraintViolationException
//        assertThatThrownBy(() -> memberRepository.save(member2))
//                .isInstanceOf(ConstraintViolationException.class);

    }


    @Test
    void 회원수정() {
        //given
        Member member = saveMember();

        //when
        member.updateAge(30);

        //then
        Member updatedMember = memberRepository.findById(member.getId()).get();
        assertThat(updatedMember).isEqualTo(member);
        assertThat(updatedMember.getAge()).isEqualTo(30);

    }

    @Test
    void 회원수정시_수정시간() {
        //given
        Member member = saveMember();

        //when
        member.updateAge(30);

        //then
        Member updatedMember = memberRepository.findById(member.getId()).get();
        assertThat(updatedMember.getUpdatedAt()).isNotNull();

    }

    @Test
    void 회원삭제() {
        //given
        Member member = saveMember();

        //when
        memberRepository.deleteById(member.getId());

        //then
        assertThat(memberRepository.findById(member.getId())).isEmpty();

    }

    @Test
    void existsByUsername() {
        //given
        Member member = saveMember();

        //then
        assertThat(memberRepository.existsByUsername(member.getUsername())).isTrue();

    }

    @Test
    void existsByEmail() {
        //given
        Member member = saveMember();

        //then
        assertThat(memberRepository.existsByEmail(member.getEmail())).isTrue();


    }

    @Test
    void findByUsername() {
        //given
        Member member = saveMember();

        //when
        memberRepository.save(member);

        //then
        assertThat(memberRepository.findByUsername(member.getUsername())).isNotNull();

    }

    @Test
    void findByEmail() {
        //given
        Member member = saveMember();

        //when, then
        assertThat(memberRepository.findByEmail(member.getEmail())).isNotNull();

    }

    private Member saveMember() {
        Member member = Member.createMember("username", "password123!", "email@email.com", "nick", 20, null);
        member.addUserRole();
        memberRepository.save(member);
        return member;
    }

    private Member saveMember(String username, String email, String nickname) {
        Member member = Member.createMember(username, "password123!", email, nickname, 20, null);
        member.addUserRole();
        memberRepository.save(member);
        return member;
    }


}