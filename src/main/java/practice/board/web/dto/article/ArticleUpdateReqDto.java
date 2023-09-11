package practice.board.web.dto.article;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Data
public class ArticleUpdateReqDto {

    @NotBlank
    private String checkPassword;  //글 작성자의 password

    private String title;  //TODO Optional<String> 으로 받아오면 더 좋겠다? update 처리할 때..그냥 코드에서 Optional.ofNullable() 로 감싸주면 되지 않을까?
    private String content;
    private MultipartFile uploadFile;

}
