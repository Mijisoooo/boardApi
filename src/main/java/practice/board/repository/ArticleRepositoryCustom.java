package practice.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import practice.board.domain.Article;
import practice.board.domain.ArticleSearchCond;

public interface ArticleRepositoryCustom {
    Page<Article> searchArticleByCond(ArticleSearchCond cond, Pageable pageable);
}
