package practice.board.domain.article.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import practice.board.domain.Article;
import practice.board.domain.Member;

class ArticleServiceTest {

    @Test
    void save() {
        Member member = Member.builder().username("username").password("password").email("email").nickname("nickname").build();

        Article article = Article.createArticle(member, "title", "content");
        
        Assertions.assertThat(article).isNotNull();

    }

}