package practice.board.web.dto.comment;

import lombok.Data;

@Data
public class CommentSaveReqDto {

    private Long articleId;
    private Long writerId;
    private String content;


}
