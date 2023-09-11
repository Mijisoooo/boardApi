package practice.board.web.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MemberDeleteDto {

    @NotBlank
    private String password;
}
