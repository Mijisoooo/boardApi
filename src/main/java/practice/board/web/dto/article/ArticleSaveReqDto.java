package practice.board.web.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import practice.board.domain.Article;

import java.util.Optional;


@Data
public class ArticleSaveReqDto {

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private Optional<MultipartFile> uploadFile;


    public static Article from(ArticleSaveReqDto dto) {
        return Article.createArticle(dto.getTitle(), dto.getContent());
    }

}
