package practice.board.web.dto.article;

import lombok.Data;

@Data
public class ArticleUpdateReqDto {

    private String title;
    private String content;
    private String filePath;

}
