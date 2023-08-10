package practice.board.web.dto.article;

import lombok.*;
import practice.board.domain.Article;

import java.time.LocalDateTime;

@Data
public class ArticleResDto {

    private Long id;
    private Long writerId;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private int viewCount;
    private int likes;
    private int dislikes;

    public ArticleResDto(Article article) {
        this.id = article.getId();
        this.writerId = article.getWriter().getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.viewCount = article.getViewCount();
        this.likes = article.getLikes();
        this.dislikes = article.getDislikes();
    }



}
