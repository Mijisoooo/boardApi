package practice.board.web.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {

    private Long memberId;  //member 의 id 값
    private String accessToken;
    private String refreshToken;

}
