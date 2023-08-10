package practice.board.web.dto.member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberLoginResDto {

    private Long id;
    private String username;

}
