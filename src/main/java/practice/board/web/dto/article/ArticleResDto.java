package practice.board.web.dto.article;

import lombok.*;
import practice.board.domain.Article;
import practice.board.domain.Comment;
import practice.board.web.dto.comment.CommentResDto;
import practice.board.web.dto.member.MemberResDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.*;

@Data
@AllArgsConstructor(access = PROTECTED)
@Builder(access = PROTECTED)
public class ArticleResDto {

    private Long id;
//    private Long writerId;
    private String writerNickname;  //작성자 닉네임
    private String title;
    private String content;
    private String filePath;  //업로드 파일 경로
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private int viewCount;
    private int likes;
    private int dislikes;

    @Builder.Default
    private List<Long> commentIdList = new ArrayList<>();

//    @Builder.Default
//    private List<CommentResDto> commentDtoList = new ArrayList<>();  //댓글 리스트  //TODO 이렇게 전부를 가져올 필요가 없겠다. commentId 정도만 가져오면 될듯



    /**
     * Article -> ArticleResDto 변환 메서드
     */
    public static ArticleResDto from(Article article) {
        ArticleResDto dto = ArticleResDto.builder()
                .id(article.getId())
                .writerNickname(article.getWriter().getNickname())
                .title(article.getTitle())
                .content(article.getContent())
                .filePath(article.getFilePath())
                .viewCount(article.getViewCount())
                .likes(article.getLikes())
                .dislikes(article.getDislikes())
                .build();

        //commentId 가져오기
        List<Long> commentIdList = article.getCommentList().stream()
                .map(Comment::getId)
                .collect(toList());

        dto.setCommentIdList(commentIdList);

        return dto;


        //TODO 아래 방식 생각 더 필요. 굳이 commentListMap 을 만들 필요가 있을까? 그냥 depth=0 인 댓글만 가져오면 되지 않나. commentDtoList 필드를 어떻게 구성할 것인지부터 생각해보자
        //TODO -> 이후에 컨트롤러를 어떻게 만들어나가는지에 따라 바뀔 수 있음
        //댓글을 부모댓글 - 본댓글 Map 으로 생성
        //article.getCommentList() -> 모든 depth 의 댓글이 조회됨
        //key : 모든 부모 댓글 / value : key 의 자식댓글
//        Map<Comment, List<Comment>> commentListMap = article.getCommentList().stream()
//                .filter(comment -> comment.getParent() != null)
//                .collect(groupingBy(Comment::getParent));
//
//        //commentDtoList 생성 : depth=0 인 댓글들을 CommentResDto 로 가져오고,
//        dto.setCommentDtoList(commentListMap.keySet().stream()
//                .filter(comment -> comment.getParent() == null)
//                .map(CommentResDto::from)
//                .toList());

    }







}
