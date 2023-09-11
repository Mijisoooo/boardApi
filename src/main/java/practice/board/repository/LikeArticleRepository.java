package practice.board.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import practice.board.domain.Article;
import practice.board.domain.LikeArticle;
import practice.board.domain.Member;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikeArticleRepository {

    private final EntityManager em;

    public void save(LikeArticle likeArticle) {
        em.persist(likeArticle);
    }

    public LikeArticle findById(Long id) {
        return em.find(LikeArticle.class, id);
    }

    public Optional<LikeArticle> findByArticleAndMember(Article article, Member member) {
        try {
            LikeArticle result = em.createQuery("select l from LikeArticle l where l.article = :article and l.member = :member", LikeArticle.class)
                    .setParameter("article", article)
                    .setParameter("member", member)
                    .getSingleResult();

            return Optional.ofNullable(result);
        } catch(NoResultException e) {
            return Optional.empty();
        }
    }

    public void delete(LikeArticle likeArticle){
        em.remove(likeArticle);
    }




}
