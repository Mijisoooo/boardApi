package practice.board.web.dto.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import practice.board.domain.*;
import practice.board.web.dto.article.ArticleResDto;
import practice.board.web.dto.comment.CommentResDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.*;

@Data
@AllArgsConstructor(access = PROTECTED)
@Builder(access = PROTECTED)
public class MemberResDto {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private Integer age;
    private Address address;

    @Builder.Default
    private List<Long> articleIdList = new ArrayList<>();  //TODO 다른 처리방법 필요 (아래 변수들 모두) - controller 가 아닌 Service단에서 변환 ...?

    @Builder.Default
    private List<Long> commentIdList = new ArrayList<>();

    @Builder.Default
    private List<Long> likeArticleIdList = new ArrayList<>();


//    @Builder.Default
//    private List<ArticleResDto> articleDtoList = new ArrayList<>();

//    @Builder.Default
//    private List<CommentResDto> commentDtoList = new ArrayList<>();


    /**
     * Member -> MemberResDto 변환 메서드
     */
    public static MemberResDto from(Member member) {
        MemberResDto dto = MemberResDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .age(member.getAge())
                .address(member.getAddress())
                .build();

        //articleIdList 생성
        if (!member.getArticleList().isEmpty()) {
            List<Long> articleIdList = member.getArticleList().stream()
                    .map(Article::getId)
                    .toList();
            dto.setArticleIdList(articleIdList);
        }

        //commentIdList 생성
        if (!member.getCommentList().isEmpty()) {
            List<Long> commentIdList = member.getCommentList().stream()
                    .map(Comment::getId)
                    .toList();
            dto.setCommentIdList(commentIdList);
        }

        //likeArticleIdList 생성
        if (!member.getLikeArticleList().isEmpty()) {
            List<Long> likeArticleIdList = member.getLikeArticleList().stream()
                    .map(LikeArticle::getId)
                    .toList();
            dto.setLikeArticleIdList(likeArticleIdList);
        }

        return dto;


//        //articleDtoList 설정
//        member.getArticleList().forEach(article ->
//                dto.getArticleDtoList().add(ArticleResDto.from(article)));
//
//        //commentDtoList 설정
//        member.getCommentList().forEach(comment ->
//                dto.getCommentDtoList().add(CommentResDto.from(comment)));

    }


}
