package practice.board.domain.comment.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.board.service.ArticleService;
import practice.board.domain.Comment;
import practice.board.domain.Member;
import practice.board.service.CommentService;
import practice.board.service.MemberService;

import static org.assertj.core.api.Assertions.*;


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

    void clear() {
        em.flush();
        em.clear();
    }

    /**
     * 자식 없는 댓글 생성
     */
    Long saveComment() {
        Member member = Member.builder()  //@Builder를 사용하면 기존 세팅된 초기화값 무시됨 (해당 필드에 @Builder.default 추가 필요)
                .username("username")
                .password("password")
                .email("email")
                .nickname("nickname")
                .build();
        Long memberId = memberService.saveMember(member);
        Long articleId = articleService.save(member.getId(), "title", "content");
        return commentService.save(articleId, memberId, "content", null);
    }

    /**
     * 자식댓글 생성
     */
    Long saveChildComment(Long parentCommentId) {
        Comment parentComment = commentService.findById(parentCommentId).get();
        return commentService.save(parentComment.getArticle().getId(), parentComment.getWriter().getId(), "content-child", parentCommentId);
    }

    @Test
    void save() {
        Long commentId = saveComment();
        Comment comment = commentService.findById(commentId).get();

        Comment subComment = Comment.createComment(comment.getArticle(), comment.getWriter(), "content-child", comment);
        em.persist(subComment);

        assertThat(comment.getDepth()).isEqualTo(0);
        assertThat(subComment.getDepth()).isEqualTo(1);
        assertThat(comment.getChildList()).isNotNull();
        assertThat(comment.getChildList()).contains(subComment);
    }

    @DisplayName("자식댓글이 있는 경우 부모댓글 화면상 삭제, 자식댓글 삭제 후에는 화면상 삭제된 부모댓글이 db에서 삭제된다.")
    @Test
    void delete_parentComment() {
        //given
        Long parentCommentId = saveComment();
        Long childCommentId = saveChildComment(parentCommentId);

        //when, then
        commentService.delete(parentCommentId);
        assertThat(commentService.findById(parentCommentId).get().isRemoved()).isTrue();
        assertThat(commentService.findById(parentCommentId).get().getContent()).isEqualTo("삭제된 댓글입니다.");
        clear();

        //자식댓글 삭제 시 부모댓글 db에서 삭제
        commentService.delete(childCommentId);
        clear();
        assertThat(commentService.findById(parentCommentId).orElse(null)).isNull();
    }

    @DisplayName("자식댓글 없는 경우 댓글 삭제 - db에서 삭제됨")
    @Test
    void delete_comment() {
        //given
        Long commentId = saveComment();

        //when
        commentService.delete(commentId);
        clear();

        //then
        assertThat(commentService.findById(commentId).orElse(null)).isNull();  //TODO 위 테스트랑 비슷한데 왜 null이 아닌거야???
    }

    @DisplayName("3계층의 댓글(댓글-대댓글-대대댓글)인 경우, (1)댓글 삭제 시 화면상삭제 (2)대대댓글 삭제 시 db삭제 (3)대댓글 삭제 시 db삭제 + 댓글도 db삭제")
    @Test
    void delete_comment_3depth() {
        //given
        Long commentId = saveComment();  //댓글
        Long childCommentId = saveChildComment(commentId);  //대댓글
        Long grandChildCommentId = saveChildComment(childCommentId);  //대대댓글

        //when, then
        //(1)댓글 삭제 시 tempDelete(화면상삭제)
        commentService.delete(commentId);
        clear();
        assertThat(commentService.findById(commentId).get()).isNotNull();
        assertThat(commentService.findById(commentId).get().isRemoved()).isTrue();
        assertThat(commentService.findById(commentId).get().getContent()).isEqualTo("삭제된 댓글입니다.");

        //(2)대댓글 삭제 시 tempDelete
        commentService.delete(childCommentId);
        clear();
        assertThat(commentService.findById(childCommentId).get()).isNotNull();
        assertThat(commentService.findById(childCommentId).get().isRemoved()).isTrue();
        assertThat(commentService.findById(childCommentId).get().getContent()).isEqualTo("삭제된 댓글입니다.");

        //(3)대대댓글 삭제 시 db삭제 + 대댓글,댓글 모두 db삭제
        commentService.delete(grandChildCommentId);
        clear();
        assertThat(commentService.findById(grandChildCommentId).orElse(null)).isNull();
        assertThat(commentService.findById(childCommentId).orElse(null)).isNull();
        assertThat(commentService.findById(commentId).orElse(null)).isNull();

    }


}