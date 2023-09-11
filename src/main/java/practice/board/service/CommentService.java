package practice.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Article;
import practice.board.domain.Comment;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.repository.ArticleRepository;
import practice.board.repository.CommentRepository;
import practice.board.repository.MemberRepository;
import practice.board.web.dto.comment.CommentResDto;

import java.util.List;

import static practice.board.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleService articleService;
    private final MemberService memberService;

    /**
     * 저장
     */
    //TODO Comment.createComment() 랑 코드 중복..... 그렇지만 괜찮은 것 같다.
    @Transactional
    public Long saveComment(Long articleId, Long memberId, String content, Long parentCommentId) {
        Article article = articleService.findById(articleId, false);
        Member member = memberService.findById(memberId);
        Comment parentComment = parentCommentId != null ? findById(parentCommentId) : null;
        Comment comment = Comment.createComment(article, member, content, parentComment);

        commentRepository.save(comment);
        return comment.getId();
    }


    /**
     * 수정 (content)
     */
    @Transactional
    public void update(Long id, String content) {
        Comment comment = findById(id);
        comment.updateContent(content);
    }


    /**
     * 삭제
     */
    @Transactional
    public void delete(Long id) {
        //comment 조회
        Comment comment = findById(id);

        //자식댓글 없는 경우
        if (comment.isChildListEmpty()) {

            Comment parentComment = comment.getParent();

            //부모댓글이 있고 화면상 삭제된 상태인 경우
            while (parentComment != null && parentComment.isRemoved()) {
                Long parentId = parentComment.getId();
                parentComment = parentComment.getParent();
                commentRepository.deleteById(parentId);  //부모댓글 : db 에서 삭제
            }

            commentRepository.deleteById(id);  //해당 댓글 : db 에서 삭제
        }
        //자식댓글 있는 경우
        else {
            comment.tempDelete();  //해당 댓글 : 화면상 삭제
        }
    }


    /**
     * id로 조회
     */
    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(() ->
                new ApiException(COMMENT_NOT_FOUND));
    }


    /**
     * CommentResDto 로 변환
     */
    public CommentResDto toCommentResDto(long commentId) {
        Comment comment = findById(commentId);
        return CommentResDto.from(comment);
    }
}
