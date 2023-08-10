package practice.board.domain.member.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;
import practice.board.domain.Role;
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


    @Test
    void 회원저장_성공() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

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
        Member member = Member.builder()
                .username("test")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

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
        Member member = Member.builder()
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        em.persist(member);

        //when, then
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생
    }



    @Test
    void 회원저장_이름X_오류() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        em.persist(member);

        //when, then
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생
    }

    @Test
    void 회원저장_닉네임X_오류() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("test")
                .email("test@test.com")
                .age(20)
                .role(Role.USER).build();

        em.persist(member);

        //when, then
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생

    }

    @Test
    void 회원저장_이메일X_오류() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("test")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        em.persist(member);

        //when, then
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생

    }

    @Test
    void 회원저장_비밀번호X_오류() {
        //given
        Member member = Member.builder()
                .username("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        em.persist(member);

        //when, then
        assertThrows(Exception.class, () -> em.flush()); //ConstraintViolationException 발생

    }

    @Test
    void 회원저장_중복아이디_오류() {
        //given
        Member member1 = Member.builder()
                .username("test")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();


        Member member2 = Member.builder()
                .username("test")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        //when
        memberRepository.save(member1);
        memberRepository.save(member2);

        //then
        assertThrows(Exception.class, () -> em.flush());  //org.hibernate.exception.ConstraintViolationException
//        assertThatThrownBy(() -> memberRepository.save(member2))
//                .isInstanceOf(ConstraintViolationException.class);

    }

    @Test
    void 회원저장_중복이메일_오류() {
        //given
        Member member1 = Member.builder()
                .username("test1")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();


        Member member2 = Member.builder()
                .username("test2")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        //when
        memberRepository.save(member1);
        memberRepository.save(member2);

        //then
        assertThrows(Exception.class, () -> em.flush());  //org.hibernate.exception.ConstraintViolationException
//        assertThatThrownBy(() -> memberRepository.save(member2))
//                .isInstanceOf(ConstraintViolationException.class);

    }


    @Test
    void 회원수정() {
        //given
        Member member = Member.builder()
                .username("test1")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        memberRepository.save(member);

        //when
        member.updateAge(30);

        //then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 회원이 없습니다. memberId=" + member.getId()));
        assertThat(updatedMember).isEqualTo(member);
        assertThat(updatedMember.getAge()).isEqualTo(30);

    }

    @Test
    void 회원수정시_수정시간() {
        //given
        Member member = Member.builder()
                .username("test1")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        memberRepository.save(member);

        //when
        member.updateAge(30);

        //then
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow(() ->
                new NoSuchElementException("해당 회원이 없습니다. memberId=" + member.getId()));
        assertThat(updatedMember.getModifiedAt()).isNotNull();

    }

    @Test
    void 회원삭제() {
        //given
        Member member = Member.builder()
                .username("test1")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        memberRepository.save(member);

        //when
        memberRepository.deleteById(member.getId());

        //then
        assertThat(memberRepository.findById(member.getId())).isNull();

    }

    @Test
    void existsByUsername() {
        //given
        Member member = Member.builder()
                .username("test1")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        //when
        memberRepository.save(member);

        //then
        assertThat(memberRepository.existByUsername(member.getUsername())).isTrue();

    }

    @Test
    void existsByEmail() {
        //given
        Member member = Member.builder()
                .username("test1")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        //when
        memberRepository.save(member);

        //then
        assertThat(memberRepository.existsByEmail(member.getEmail())).isTrue();


    }

    @Test
    void findByUsername() {
        //given
        Member member = Member.builder()
                .username("test1")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        //when
        memberRepository.save(member);

        //then
        assertThat(memberRepository.findByUsername(member.getUsername())).isNotNull();

    }

    @Test
    void findByEmail() {
        //given
        Member member = Member.builder()
                .username("test1")
                .password("test")
                .email("test@test.com")
                .nickname("test")
                .age(20)
                .role(Role.USER).build();

        //when
        memberRepository.save(member);

        //then
        assertThat(memberRepository.findByEmail(member.getEmail())).isNotNull();

    }


}