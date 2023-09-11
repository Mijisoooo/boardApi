package practice.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import static jakarta.persistence.FetchType.*;
import static org.hibernate.annotations.OnDeleteAction.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class DislikeArticle extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "article_id")
    @OnDelete(action = CASCADE)  //단방향 연관관계에서 cascade 옵션을 주고싶을 때 사용 (article 이 삭제되면 이 데이터도 삭제됨)
    private Article article;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = CASCADE)  //단방향 연관관계에서 cascade 옵션을 주고싶을 때 사용 (member 가 삭제되면 이 데이터도 삭제됨)
    private Member member;

    @Builder.Default
    @NotNull
    private boolean status = true;  //true: 싫어요, false: 싫어요 취소  //TODO 없애도 될듯. 싫어요 취소하면 이 데이터 자체가 사라지니까



    //== 생성 메서드 ==//
    public static DislikeArticle create(Article article, Member member) {
         return DislikeArticle.builder()
                .article(article)
                .member(member)
                .build();
    }


}
