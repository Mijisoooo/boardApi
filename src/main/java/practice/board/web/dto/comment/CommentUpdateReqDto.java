package practice.board.web.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdateReqDto {

    @NotBlank
    private String content;
}
