package practice.board.web.dto.article;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArticleDeleteReqDto {

    private String checkPassword;  //TODO @NotBlank 안함 - ADMIN의 경우 어떻게할지 고민중이라

}
