package practice.board.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleSearchCond {

    private String title;
    private String content;
    private String nickname;

}
