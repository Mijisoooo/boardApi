package practice.board.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import practice.board.domain.Article;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleRepository {

    private final EntityManager em;

    /**
     * 저장
     */
    public Long save(Article article) {
        em.persist(article);
        return article.getId();
    }

    /**
     * id로 조회
     */
    public Article findById(Long id) {
        return em.find(Article.class, id);
    }


    /**
     * 전체 조회
     */
    public List<Article> findAll() {
        return em.createQuery("select a from Article a", Article.class)
                .getResultList();
    }


    /**
     * 검색조건으로 조회 - 동적쿼리 사용
     */


    /**
     * 수정 (title, content, filePath (제목, 내용, 첨부파일) 만 수정 가능)
     */
//    public void update(Long id, Article updateParam) {
//        Article article = findById(id);
//        article.update(updateParam.getTitle(), updateParam.getContent());
//    }



    /**
     * 수정 (title, content, filePath 를 파라미터로 받음)
     */
    public void update(Long id, String newTitle, String newContent, String newFilePath) {
        Article article = findById(id);
        article.updateTitle(newTitle);
        article.updateContent(newContent);
        article.updateFilePath(newFilePath);
    }


    /**
     * id로 삭제
     */
    public void deleteById(Long id) {
        Article findArticle = findById(id);
        em.remove(findArticle);
    }


}
