package practice.board.web.dto.article;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class ArticleSaveReqDto {

    @NotEmpty
    private Long writerId;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;



}
