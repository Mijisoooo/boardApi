package practice.board.web.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import practice.board.domain.Comment;
import practice.board.response.Response;
import practice.board.service.ArticleService;
import practice.board.service.CommentService;
import practice.board.web.dto.comment.CommentResDto;
import practice.board.web.dto.comment.CommentUpdateReqDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentApiController {

    private final CommentService commentService;
    private final ArticleService articleService;

    /**
     * 모든 Comment 조회
     */
    @GetMapping("/comments")
    public Response comments() {
        List<Comment> comments = commentService.findComments();
        List<CommentResDto> dtos = comments.stream()
                .map(c -> new CommentResDto(c))
                .collect(Collectors.toList());

        return Response.success(dtos);
    }

    /**
     * 해당 글의 Comment 조회
     */
    @GetMapping("/articles/{id}/comments")
    public Response commentsByArticleId(@PathVariable Long id) {
        List<Comment> comments = commentService.findCommentsByArticleId(id);
        List<CommentResDto> dtos = comments.stream()
                .map(c -> new CommentResDto(c))
                .collect(Collectors.toList());

        return Response.success(dtos);
    }


    /**
     * 저장
     */
//    @PostMapping("/comments")
//    public CommentResDto saveComment(@RequestBody CommentSaveReqDto request) {
//
//        Long savedId = commentService.save(request.getArticleId(), request.getWriterId(), request.getContent(), );
//        Comment comment = commentService.findById(savedId);
//
//        return new CommentResDto(comment);
//    }


    /**
     * 수정
     */
    @PatchMapping("/comments/{id}")
    public Response updateComment(@PathVariable Long id, @RequestBody CommentUpdateReqDto request) {

        //TODO 댓글 작성자, 관리자만 수정 가능
        commentService.update(id, request.getContent());
        Comment comment = commentService.findById(id).orElseThrow(() -> new NoSuchElementException("댓글이 없습니다."));
        CommentResDto dto = new CommentResDto(comment);
        return Response.success(dto);
    }

    /**
     * 삭제
     */
    @DeleteMapping("/comments/{id}")
    public Response deleteComment(@PathVariable Long id) {

        //TODO 댓글 작성자, 관리자만 삭제 가능
        commentService.delete(id);
        return Response.success();

    }



}
