package practice.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Lob
    @NotNull
    private String content;

    private int likes;

    private int dislikes;

    private boolean isRemoved = false;

    private int depth;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> childList = new ArrayList<>();  //부모 댓글을 삭제해도 자식 댓글은 남아있음



    //== 수정 ==//
    public void updateContent(String content) {
        this.content = content;
    }

    //== 화면상 삭제  ==//
    public void tempDelete() {
        this.isRemoved = true;
        this.updateContent("삭제된 댓글입니다.");
    }


    //== 연관관계 메서드 ==//
    public void setWriter(Member member) {
        this.writer = member;
        member.getCommentList().add(this);
    }

    public void setArticle(Article article) {
        this.article = article;
        article.getCommentList().add(this);
    }

    public void setParentAndDepth(Comment parentComment) {
        this.parent = parentComment;
        parentComment.getChildList().add(this);
        this.depth = parentComment.getDepth() + 1;
    }


    //== 생성 메서드 ==//

    /**
     * parentComment 없으면 null 전달
     */
    public static Comment createComment(Article article, Member member, String content, Comment parentComment) {  //TODO @Builder 삭제??
        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setWriter(member);
        comment.content = content;
        comment.likes = 0;
        comment.dislikes = 0;
        comment.isRemoved = false;
        if (parentComment != null) {
            comment.setParentAndDepth(parentComment);
        }
        else {  //parentComment = null 인 경우
            comment.depth = 0;
        }
        return comment;
    }


    //== 비즈니스 로직 ==//
    public int addLikes() {
        likes++;
        return likes;
    }

    public int addDislikes() {
        dislikes++;
        return dislikes;
    }

/*
/**
     * 모든 자식 댓글의 isRemoved = true 인지 체크
     *//*

    public boolean isAllChildrenRemoved() {
        return getChildList().stream()
                .allMatch(Comment::isRemoved);
    }
*/

    //== 조회 로직 ==//
    public boolean isChildListEmpty() {
        return getChildList().isEmpty();
    }

}
