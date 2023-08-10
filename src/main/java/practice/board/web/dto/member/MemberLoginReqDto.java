package practice.board.web.dto.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MemberLoginReqDto {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;
}
