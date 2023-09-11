package practice.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;

import static jakarta.persistence.FetchType.LAZY;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter(AccessLevel.PRIVATE)
@Entity
public class LikeArticle {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "article_id")
    @OnDelete(action = CASCADE)
    private Article article;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = CASCADE)
    private Member member;



    //== 생성 메서드 ==//
    public static LikeArticle create(Article article, Member member) {
        LikeArticle likeArticle = LikeArticle.builder()
                .article(article)
                .member(member)
                .build();

        //Member의 likeArticleList 에 넣기
        likeArticle.getMember().getLikeArticleList().add(likeArticle);

        return likeArticle;
    }



}
