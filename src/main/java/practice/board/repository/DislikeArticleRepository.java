package practice.board.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import practice.board.domain.Article;
import practice.board.domain.DislikeArticle;
import practice.board.domain.Member;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DislikeArticleRepository {

    private final EntityManager em;

    public void save(DislikeArticle dislikeArticle) {
        em.persist(dislikeArticle);
    }

    public DislikeArticle findById(Long id) {
       return em.find(DislikeArticle.class, id);
    }

    public Optional<DislikeArticle> findByArticleAndMember(Article article, Member member) {
        try {
            DislikeArticle result = em.createQuery("select d from DislikeArticle d where d.article = :article and d.member = :member", DislikeArticle.class)
                    .setParameter("article", article)
                    .setParameter("member", member)
                    .getSingleResult();

            return Optional.ofNullable(result);
        } catch(NoResultException e) {
            return Optional.empty();
        }
    }

    public void delete(DislikeArticle dislikeArticle){
        em.remove(dislikeArticle);
    }




}
