package practice.board.web.dto.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MemberDeleteDto {

    @NotEmpty
    private String password;
}
