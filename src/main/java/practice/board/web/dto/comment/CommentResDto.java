package practice.board.web.dto.comment;

import lombok.*;
import practice.board.domain.Comment;
import practice.board.web.dto.article.ArticleResDto;
import practice.board.web.dto.member.MemberResDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.*;


@Data
@AllArgsConstructor(access = PROTECTED)
@Builder(access = PROTECTED)
public class CommentResDto {

    private final static String DEFAULT_DELETE_MESSAGE = "삭제된 댓글입니다.";

    private Long id;
    private Long articleId;
//    private ArticleResDto articleDto;
//    private Long writerId;
    private MemberResDto writerDto;  //댓글작성자
    private boolean isRemoved;  //삭제된 댓글 여부
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private int likes;
    private int dislikes;
    private Integer depth;
    private CommentResDto parentDto;  //부모댓글

    @Builder.Default
    private List<CommentResDto> childDtoList = new ArrayList<>();  //자식댓글 리스트


    /**
     * Comment -> CommentResDto 변환 메서드
     */
    public static CommentResDto from(Comment comment) {

        CommentResDto dto = CommentResDto.builder()
                .id(comment.getId())
                .articleId(comment.getArticle().getId())
                .writerDto(MemberResDto.from(comment.getWriter()))
                .isRemoved(comment.isRemoved())
                .content(comment.getContent())
                .likes(comment.getLikes())
                .dislikes(comment.getDislikes())
                .depth(comment.getDepth())
                .build();

        //parentDto 설정
        Optional.ofNullable(comment.getParent()).ifPresent(parent -> dto.setParentDto(CommentResDto.from(parent)));

        //childDtoList 설정
        Optional.ofNullable(comment.getChildList()).ifPresent(
                childList ->
                        childList.forEach(child ->
                                dto.getChildDtoList().add(CommentResDto.from(child))));

        //삭제된 댓글인 경우 (화면상 삭제)
        if (comment.isRemoved()) {
            dto.setContent(DEFAULT_DELETE_MESSAGE);  //다른 필드도 수정 필요한가 (likes, dislikes 필드 사용 안하니까)
        }

        return dto;
    }
}
