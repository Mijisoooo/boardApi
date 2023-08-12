package practice.board.web.dto.jwt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResDto {

    private String accessToken;
    private String refreshToken;

}
