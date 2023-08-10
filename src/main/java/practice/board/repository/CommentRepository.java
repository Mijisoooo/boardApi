package practice.board.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import practice.board.domain.Comment;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final EntityManager em;

    /**
     * 저장
     */
    public Long save(Comment comment) {
        em.persist(comment);
        return comment.getId();
    }

    /**
     * id로 조회
     */
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(em.find(Comment.class, id));
    }

    /**
     * article에 해당하는 comment 모두 조회 TODO CommentService에 적어야하나?? 아니면 Comment의 조회 메서드에? 근데 em이 사용되었으니까 repository가 맞는 것 같기도 하고
     */
    public List<Comment> findCommentsByArticleId(Long articleId) {
        return em.createQuery("select c from Comment c where c.article = :articleId", Comment.class)
                .setParameter("articleId", articleId)
                .getResultList();
    }

    /**
     * 전체 조회
     */
    public List<Comment> findAll() {
        return em.createQuery("select c from Comment c", Comment.class)
                .getResultList();
    }


    /**
     * 수정 (content)
     */
    public void update(Long id, String content) {
        Comment comment = findById(id).get();
        comment.updateContent(content);
    }


    /**
     * id로 삭제
     */
    public void deleteById(Long id) {
        Comment comment = findById(id).get();
        em.remove(comment);
    }


}
