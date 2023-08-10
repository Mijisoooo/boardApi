package practice.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Article;
import practice.board.domain.Comment;
import practice.board.domain.Member;
import practice.board.repository.ArticleRepository;
import practice.board.repository.CommentRepository;
import practice.board.repository.MemberRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    /**
     * 저장
     */
    @Transactional
    public Long save(Long articleId, Long memberId, String content, Long parentCommentId) {
        Article article = articleRepository.findById(articleId);
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new NoSuchElementException("해당 회원이 없습니다. memberId=" + memberId));
        Comment parentComment = parentCommentId != null ? commentRepository.findById(parentCommentId).get() : null;
        Comment comment = Comment.createComment(article, member, content, parentComment);
        return commentRepository.save(comment);
    }

    /**
     * 수정
     */
    @Transactional
    public void update(Long id, String content) {
        commentRepository.update(id, content);
    }


    /**
     * 삭제
     */
    @Transactional
    public void delete(Long id) {

        Comment comment = commentRepository.findById(id).orElseThrow(() -> new NoSuchElementException("댓글이 없습니다."));

        if (comment.isChildListEmpty()) {  //자식댓글 없는 경우

            Comment parentComment = comment.getParent();

            while (parentComment != null && parentComment.isRemoved()) {  //부모댓글이 있고 화면상 삭제된 상태인 경우
                Long parentId = parentComment.getId();
                parentComment = parentComment.getParent();
                commentRepository.deleteById(parentId);  //부모댓글 : db에서 삭제
            }

            commentRepository.deleteById(id);  //해당 댓글 : db에서 삭제
        }
        else {  //자식댓글 있는 경우
            comment.tempDelete();  //해당 댓글 : 화면상 삭제
        }
    }




    /**
     * 조회
     */


    /**
     * 조회 (해당 article의 모든 comment 조회)
     */
    public List<Comment> findCommentsByArticleId(Long articleId) {
        return commentRepository.findCommentsByArticleId(articleId);
    }

    public List<Comment> findComments() {
        return commentRepository.findAll();
    }

    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }
}
