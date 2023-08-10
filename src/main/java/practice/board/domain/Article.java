package practice.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "article_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;

    @Column(length = 50)
    @NotNull
    private String title;

    @Lob
    @NotNull
    private String content;

    private String filePath;

    private int viewCount;

    private int likes;

    private int dislikes;


    @OneToMany(mappedBy = "article", cascade = ALL, orphanRemoval = true)  //게시글 삭제 시 해당 게시글의 댓글도 모두 삭제됨
    private List<Comment> commentList = new ArrayList<>();



    //== 수정 메서드 ==//
//    public void update(String title, String content, String filePath) {
//        this.title = title;
//        this.content = content;
//        this.filePath = filePath;
//    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateFilePath(String filePath) {
        this.filePath = filePath;
    }


    //== 연관관계 메서드 ==//
    public void setWriter(Member member) {
        this.writer = member;
        member.getArticleList().add(this);
    }


    //== 생성메서드 ==//
    public static Article createArticle(Member member, String title, String content) {
        Article article = new Article();
        article.setWriter(member);
        article.title = title;
        article.content = content;
        article.viewCount = 0;  //TODO 꼭 설정할 필요 없을듯? (디폴트 설정?)
        article.likes = 0;
        article.dislikes = 0;
        return article;
    }


    //== 비즈니스 로직 ==//
    public int addViewCount() {
        viewCount += 1;
        return viewCount;
    }

    public int addLikes() {
        likes++;
        return likes;
    }

    public int addDislikes() {
        dislikes++;
        return dislikes;
    }




    //== 조회 로직 ==//



}
