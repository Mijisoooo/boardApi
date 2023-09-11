package practice.board.domain.comment.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Article;
import practice.board.exception.ApiException;
import practice.board.service.ArticleService;
import practice.board.domain.Comment;
import practice.board.domain.Member;
import practice.board.service.CommentService;
import practice.board.service.MemberService;
import practice.board.web.dto.member.MemberSaveReqDto;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@Slf4j
class CommentServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    CommentService commentService;

    @Autowired
    ArticleService articleService;

    @Autowired
    MemberService memberService;

    private static final String DELETED_COMMENT_CONTENT = "삭제된 댓글입니다.";

    void clear() {
        em.flush();
        em.clear();
    }

    Long saveMember(String username, String password, String email, String nickname) {
        Member member = Member.createMember(username, password, email, nickname, null, null);
        return memberService.saveMember(MemberSaveReqDto.toDto(member));
    }

    /**
     * depth=0 인 댓글 생성
     */
    Long saveComment(Long memberId, String content) {
        Article article = Article.createArticle("title", "content");
        Long articleId = articleService.saveArticle(memberId, article, Optional.empty());
        return commentService.saveComment(articleId, memberId, content, null);
    }

    /**
     * 자식댓글 생성
     */
    Long saveChildComment(Long memberId, String content, Long parentCommentId) {
        Comment parentComment = commentService.findById(parentCommentId);
        return commentService.saveComment(parentComment.getArticle().getId(), memberId, content, parentCommentId);
    }


    @Test
    void save() {
        Long memberId = saveMember("username", "Password123!", "email@email.com", "nickname");
        Long commentId = saveComment(memberId, "content");
        Comment comment = commentService.findById(commentId);

        Comment subComment = Comment.createComment(comment.getArticle(), comment.getWriter(), "content-child", comment);
        em.persist(subComment);

        assertThat(comment.getDepth()).isEqualTo(0);
        assertThat(subComment.getDepth()).isEqualTo(1);
        assertThat(comment.getChildList()).isNotNull();
        assertThat(comment.getChildList()).contains(subComment);
    }

    @DisplayName("자식댓글이 있는 경우 부모댓글 화면상 삭제, 자식댓글 삭제 후에는 화면상 삭제된 부모댓글이 db 에서 삭제된다.")
    @Test
    void delete_parentComment() {
        //given
        Long memberId1 = saveMember("username1", "Password123!", "email1@email.com", "nickname1");
        Long parentCommentId = saveComment(memberId1, "content");

        Long memberId2 = saveMember("username2", "Password1234!", "email2@email.com", "nickname2");
        Long childCommentId = saveChildComment(memberId2, "child-content", parentCommentId);

        //when, then
        commentService.delete(parentCommentId);
        assertThat(commentService.findById(parentCommentId).isRemoved()).isTrue();
        assertThat(commentService.findById(parentCommentId).getContent()).isEqualTo(DELETED_COMMENT_CONTENT);
        clear();

        //자식댓글 삭제 시 부모댓글 db 에서 삭제
        commentService.delete(childCommentId);
        clear();
        assertThat(commentService.findById(parentCommentId)).isNull();
    }

    @DisplayName("자식댓글 없는 경우 댓글 삭제 - db 에서 삭제됨")
    @Test
    void delete_comment() {
        //given
        Long memberId = saveMember("username1", "Password123!", "email1@email.com", "nicky");
        Long commentId = saveComment(memberId, "content");

        //when
        commentService.delete(commentId);
        clear();

        //then
        assertThatThrownBy(() -> commentService.findById(commentId))
                .isInstanceOf(ApiException.class);
        assertThrows(Exception.class, () -> commentService.findById(commentId));
    }

    @DisplayName("3계층의 댓글(댓글-대댓글-대대댓글)인 경우, (1)댓글 삭제 시 화면상삭제 (2)대대댓글 삭제 시 db삭제 (3)대댓글 삭제 시 db삭제 + 댓글도 db삭제")
    @Test
    void delete_comment_3depth() {
        //given
        Long memberId1 = saveMember("username1", "Password123!", "email1@email.com", "nickname1");
        Long commentId = saveComment(memberId1, "content");  //depth=0 댓글

        Long memberId2 = saveMember("username2", "Password1234!", "email2@email.com", "nickname2");
        Long childCommentId = saveChildComment(memberId2, "content", commentId);  //depth=1 대댓글

        Long grandChildCommentId = saveChildComment(memberId1, "content content", childCommentId);  //depth=2 대대댓글

        //when, then
        //(1) 댓글 삭제 시 tempDelete(화면상삭제)
        commentService.delete(commentId);
        clear();
        assertThat(commentService.findById(commentId)).isNotNull();
        assertThat(commentService.findById(commentId).isRemoved()).isTrue();
        assertThat(commentService.findById(commentId).getContent()).isEqualTo(DELETED_COMMENT_CONTENT);

        //(2) 대댓글 삭제 시 tempDelete
        commentService.delete(childCommentId);
        clear();
        assertThat(commentService.findById(childCommentId)).isNotNull();
        assertThat(commentService.findById(childCommentId).isRemoved()).isTrue();
        assertThat(commentService.findById(childCommentId).getContent()).isEqualTo(DELETED_COMMENT_CONTENT);

        //(3) 대대댓글 삭제 시 db 삭제 + 대댓글,댓글 모두 db 삭제
        commentService.delete(grandChildCommentId);
        clear();
        assertThat(commentService.findById(grandChildCommentId)).isNull();
        assertThat(commentService.findById(childCommentId)).isNull();
        assertThat(commentService.findById(commentId)).isNull();
    }


    @DisplayName("혼합 계층 댓글 삭제 테스트")
    @Test
    void delete_test() {
        //given
        Long memberId1 = saveMember("username1", "Password123!", "email1@email.com", "nick1");
        Long memberId2 = saveMember("username2", "Password1234!", "email2@email.com", "nick2");

        Long commentId1 = saveComment(memberId1, "content1 depth=0");  //depth=0
        Long commentId2 = saveChildComment(memberId2, "content2 depth=1", commentId1);  //depth=1
        Long commentId7 = saveChildComment(memberId1, "content7 depth=1", commentId1);  //depth=1


        Long commentId3 = saveChildComment(memberId1, "content3 depth=2", commentId2);  //depth=2
        Long commentId4 = saveChildComment(memberId1, "content4 depth=3", commentId3);  //depth=3
        Long commentId8 = saveChildComment(memberId2, "content8 depth=3", commentId2);  //depth=3
        Long commentId9 = saveChildComment(memberId1, "content9 depth=4", commentId8);  //depth=4

        Long commentId5 = saveComment(memberId2, "content4 depth=0");  //depth=0
        Long commentId6 = saveChildComment(memberId2, "content5 depth=1", commentId5);  //depth=1


        /* [댓글 계층 구조]
        commentId1 - commentId2 - commentId3 - commentId4
                                - commentId8 - commentId9
                   - commentId7
        commentId5 - commentId6
        */


        //when, then
        //depth 확인
        assertThat(commentService.findById(commentId1).getDepth()).isEqualTo(0);
        assertThat(commentService.findById(commentId2).getDepth()).isEqualTo(1);
        assertThat(commentService.findById(commentId3).getDepth()).isEqualTo(2);
        assertThat(commentService.findById(commentId4).getDepth()).isEqualTo(3);
        assertThat(commentService.findById(commentId5).getDepth()).isEqualTo(0);
        assertThat(commentService.findById(commentId6).getDepth()).isEqualTo(1);
        assertThat(commentService.findById(commentId7).getDepth()).isEqualTo(1);
        assertThat(commentService.findById(commentId8).getDepth()).isEqualTo(2);
        assertThat(commentService.findById(commentId9).getDepth()).isEqualTo(3);

        //(1) 자식댓글 있는 경우 - 댓글 삭제 시 tempDelete(화면상삭제)
        //comment1
        commentService.delete(commentId1);
        Comment comment1 = commentService.findById(commentId1);
        assertThat(comment1).isNotNull();
        assertThat(comment1.isRemoved()).isTrue();
        assertThat(comment1.getContent()).isEqualTo(DELETED_COMMENT_CONTENT);

        //comment5
        commentService.delete(commentId5);
        Comment comment5 = commentService.findById(commentId5);
        assertThat(comment5).isNotNull();
        assertThat(comment5.isRemoved()).isTrue();
        assertThat(comment5.getContent()).isEqualTo(DELETED_COMMENT_CONTENT);

        //comment2
        commentService.delete(commentId2);
        Comment comment2 = commentService.findById(commentId2);
        assertThat(comment2).isNotNull();
        assertThat(comment2.isRemoved()).isTrue();
        assertThat(comment2.getContent()).isEqualTo(DELETED_COMMENT_CONTENT);

        //comment3
        commentService.delete(commentId3);
        Comment comment3 = commentService.findById(commentId3);
        assertThat(comment3).isNotNull();
        assertThat(comment3.isRemoved()).isTrue();
        assertThat(comment3.getContent()).isEqualTo(DELETED_COMMENT_CONTENT);

        //comment8
        commentService.delete(commentId8);
        Comment comment8 = commentService.findById(commentId8);
        assertThat(comment8).isNotNull();
        assertThat(comment8.isRemoved()).isTrue();
        assertThat(comment8.getContent()).isEqualTo(DELETED_COMMENT_CONTENT);


        //(2) 자식댓글 없는 경우 - 댓글 삭제 시 db 삭제
        //comment4
        commentService.delete(commentId4);
        assertThatThrownBy(() -> commentService.findById(commentId4))
                .isInstanceOf(ApiException.class);

        //comment9
        commentService.delete(commentId9);
        assertThatThrownBy(() -> commentService.findById(commentId9))
                .isInstanceOf(ApiException.class);

        //comment7
        commentService.delete(commentId7);
        assertThatThrownBy(() -> commentService.findById(commentId7))
                .isInstanceOf(ApiException.class);

        //comment6
        commentService.delete(commentId6);
        assertThatThrownBy(() -> commentService.findById(commentId6))
                .isInstanceOf(ApiException.class);

        //(3) 자식댓글이 있었다가 사라진 경우 - db 삭제됨
        assertThatThrownBy(() -> commentService.findById(commentId1))
                .isInstanceOf(ApiException.class);

        assertThatThrownBy(() -> commentService.findById(commentId5))
                .isInstanceOf(ApiException.class);

        assertThatThrownBy(() -> commentService.findById(commentId2))
                .isInstanceOf(ApiException.class);

        assertThatThrownBy(() -> commentService.findById(commentId3))
                .isInstanceOf(ApiException.class);

        assertThatThrownBy(() -> commentService.findById(commentId8))
                .isInstanceOf(ApiException.class);

    }


}