package practice.board.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Member;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    /**
     * 등록
     */
    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }
//    public Long save(Member member) {
//        if (findByEmail(member.getEmail()).isEmpty() || findByEmail(member.getEmail()) == null) { //TODO unique 제약조건 걸었는데 그 에러보다 아래 IllegalStateException이 우선하네? - 검증 공부
//            em.persist(member);
//            return member.getId();
//        }
//        throw new IllegalStateException("회원가입 불가 (중복 email), email=" + member.getEmail());
//    }


    /**
     * 조회
     */
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public Optional<Member> findByEmail(String email) {
        List<Member> members = em.createQuery("select m from Member m where m.email = :email", Member.class)
                .setParameter("email", email)
                .getResultList();
        return members.stream().findAny();
    }

    public Optional<Member> findByUsername(String username) {
        List<Member> members = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
        return members.stream().findAny();
    }

    public Optional<Member> findByNickname(String nickname) {
        List<Member> members = em.createQuery("select m from Member m where m.nickname = :nickname", Member.class)
                .setParameter("nickname", nickname)
                .getResultList();
        return members.stream().findAny();
    }

    public Optional<Member> findByRefreshToken(String refreshToken) {
        List<Member> members = em.createQuery("select m from Member m where m.refreshToken = :refreshToken", Member.class)
                .setParameter("refreshToken", refreshToken)
                .getResultList();
        return members.stream().findAny();
    }

    /**
     * 수정
     */
    @Transactional
    public void updateRefreshToken(Long memberId, String refreshToken) {
        Member member = findById(memberId).orElseThrow(() ->
                new NoSuchElementException("해당 회원이 없습니다. memberId:" + memberId));
        member.updateRefreshToken(refreshToken);
    }


    /**
     * 삭제
     */
    public void deleteById(Long id) {
        Member member = em.find(Member.class, id);
        em.remove(member);
    }

    public void delete(Member member) {
        em.remove(member);
    }


    public boolean login(String username, String password) {
        return findByUsername(username).stream()
                .anyMatch(m -> m.getPassword() == password);
    }

    public boolean existByUsername(String username) {
        List<Member> member = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
        if (member.size() != 0) { return true; }
        return false;
    }

    public boolean existsByEmail(String email) {
        List<Member> member = em.createQuery("select m from Member m where m.email = :email", Member.class)
                .setParameter("email", email)
                .getResultList();
        if (member.size() != 0) { return true; }
        return false;
    }

}
