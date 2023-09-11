package practice.board.web.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import practice.board.config.guard.JwtAuth;
import practice.board.domain.Comment;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.CommentRepository;
import practice.board.repository.MemberRepository;
import practice.board.response.Response;
import practice.board.service.ArticleService;
import practice.board.service.CommentService;
import practice.board.service.MemberService;
import practice.board.web.dto.comment.CommentResDto;
import practice.board.web.dto.comment.CommentSaveReqDto;
import practice.board.web.dto.comment.CommentUpdateReqDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentApiController {

    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;


    /**
     * 모든 Comment 조회 - 인증 필요 없음
     */
    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.OK)
    public Response<Page<CommentResDto>> comments(@RequestParam(required = false, defaultValue = "0") int page,
                                                  @RequestParam(required = false, defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Comment> comments = commentRepository.findAll(pageRequest);
        Page<CommentResDto> dto = comments.map(comment -> commentService.toCommentResDto(comment.getId()));

        return Response.success(dto);
    }


    /**
     * 해당 글의 Comment 조회 - 인증 필요 없음
     */
    @GetMapping("/articles/{id}/comments")
    @ResponseStatus(HttpStatus.OK)
    public Response<Page<CommentResDto>> commentsByArticleId(@PathVariable Long id,
                                                             @RequestParam(required = false, defaultValue = "0") int page,
                                                             @RequestParam(required = false, defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Comment> comments = commentRepository.findCommentsByArticleId(id, pageRequest);
        Page<CommentResDto> dto = comments.map(comment -> commentService.toCommentResDto(comment.getId()));

        return Response.success(dto);
    }


    /**
     * 댓글 작성 (저장) - 인증 필요
     */
    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<CommentResDto> saveComment(@Valid @RequestBody CommentSaveReqDto request, @AuthenticationPrincipal User user) {
        Member member = memberRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        Long savedId;
        if (request.getParentId() == null) {
            savedId = commentService.saveComment(request.getArticleId(), member.getId(), request.getContent(), null);
        } else {
            savedId = commentService.saveComment(request.getArticleId(), member.getId(), request.getContent(), request.getParentId());
        }

        CommentResDto dto = commentService.toCommentResDto(savedId);

        return Response.success(dto);
    }


    /**
     * 수정 - 본인만 가능
     */
    @PreAuthorize("@authService.isCommentWriter(#id)")
    @PatchMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response<CommentResDto> updateComment(@PathVariable Long id, @Valid @RequestBody CommentUpdateReqDto request) {
        commentService.update(id, request.getContent());
        CommentResDto dto = commentService.toCommentResDto(id);

        return Response.success(dto);
    }


    /**
     * 삭제 - 본인 or ADMIN 가능
     */
    @PreAuthorize("hasRole('ADMIN') or @authService.isCommentWriter(#id)")
    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response deleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return Response.success();
    }



}
