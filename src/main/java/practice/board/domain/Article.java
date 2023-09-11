package practice.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)
public class Article extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)  //??
    private Member writer;

    @Column(length = 50)
    @NotNull
    private String title;

    @Lob
    @NotNull
    private String content;

    private String filePath;

//    @ManyToOne(fetch = LAZY)
//    @JoinColumn(name = "category_id")
//    @NotNull
//    private Category category;

    @ColumnDefault("0")
    private int viewCount;

    @ColumnDefault("0")
    private int likes;

    @ColumnDefault("0")
    private int dislikes;

    @Builder.Default
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
    //TODO 아래 메서드 (member를 인수로 받는) 사용 XXX (없애고 싶은데 없애려면 ArticleRepositoryTest 코드 수정 필요)
    //TODO -> ArticleService 의 saveArticle() 과 중복 느낌
    public static Article createArticle(Member member, String title, String content) {
        Article article = Article.builder()
                .title(title)
                .content(content)
                .build();

        article.setWriter(member);

        return article;
    }

    public static Article createArticle(String title, String content) {
        Article article = Article.builder()
                .title(title)
                .content(content)
                .build();

        return article;
    }

//    public static Article createArticle(Category category, String title, String content) {
//        Article article = Article.builder()
//                .category(category)
//                .title(title)
//                .content(content)
//                .build();
//
//        return article;
//    }




    //== 비즈니스 로직 ==//
    public void increaseViewCount() {
        this.viewCount += 1;
    }

    public void increaseLikes() {
        this.likes += 1;
    }

    public void decreaseLikes() {
        this.likes -= 1;
    }

    public void increaseDislikes() {
        this.dislikes += 1;
    }

    public void decreaseDislikes() {
        this.dislikes -= 1;
    }




    //== 조회 로직 ==//



}
