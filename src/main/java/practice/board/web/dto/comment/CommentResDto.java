package practice.board.web.dto.comment;

import lombok.*;
import practice.board.domain.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Data
@AllArgsConstructor
@Builder
public class CommentResDto {


    private Long id;
    private Long articleId;
    private Long writerId;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String content;
    private int likes;
    private int dislikes;
    private Integer depth;
    private CommentResDto parent;
    private List<CommentResDto> childList = new ArrayList<>();



    public CommentResDto(Comment comment) {  //TODO 이 방식이랑 아래 to() 방식이랑 뭘 사용하면 좋을까??? 
        this.id = comment.getId();
        this.articleId = comment.getArticle().getId();
        this.writerId = comment.getWriter().getId();
        this.content = comment.getContent();
        this.likes = comment.getLikes();
        this.dislikes = comment.getDislikes();
        this.depth = comment.getDepth();
        if (comment.getParent() != null) {  //TODO null 여부 체크해야 하나? Optional + Stream 이용해서 좀더 간편한 코드 없을까?
            this.parent = new CommentResDto(comment.getParent());
        }
        if (!comment.getChildList().isEmpty()) {  //TODO null 여부 체크해야 하나?
            comment.getChildList().forEach(child -> this.childList.add(new CommentResDto(child)));
        }
    }

    /**
     * Comment -> CommentResDto
     */
    public static CommentResDto from(Comment comment) {

        CommentResDto dto = CommentResDto.builder()
                .id(comment.getId())
                .articleId(comment.getArticle().getId())
                .writerId(comment.getWriter().getId())
                .content(comment.getContent())
                .likes(comment.getLikes())
                .dislikes(comment.getDislikes())
                .depth(comment.getDepth())
                .build();

        //parentComment 설정
        Optional.ofNullable(comment.getParent()).ifPresent(parent -> dto.setParent(CommentResDto.from(parent)));

        //childList 설정
        Optional.ofNullable(comment.getChildList()).ifPresent(
                childList ->
                        childList.forEach(child ->
                                dto.getChildList().add(CommentResDto.from(child))));

        return dto;
    }
}
