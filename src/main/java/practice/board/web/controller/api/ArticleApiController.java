package practice.board.web.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import practice.board.config.guard.JwtAuth;
import practice.board.domain.Article;
import practice.board.domain.ArticleSearchCond;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.ArticleRepository;
import practice.board.response.Response;
import practice.board.service.ArticleService;
import practice.board.service.AuthService;
import practice.board.web.dto.article.ArticleDeleteReqDto;
import practice.board.web.dto.article.ArticleResDto;
import practice.board.web.dto.article.ArticleSaveReqDto;
import practice.board.web.dto.article.ArticleUpdateReqDto;

import java.util.*;

import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArticleApiController {

    private final ArticleService articleService;
    private final ArticleRepository articleRepository;
    private final AuthService authService;


    /**
     * 게시글 작성(저장) - 인증 필요 (권한 상관 없음)
     */
    @PostMapping("/articles")
    @ResponseStatus(CREATED)
    public Response<ArticleResDto> saveArticle(@Valid @RequestBody ArticleSaveReqDto request) {
        //현재 로그인한 member의 id 가져오기
        Long memberId = authService.getLoginMemberId();

        //ArticleSaveReqDto -> Article 변환
        Article article = ArticleSaveReqDto.from(request);

        //TODO 컨트롤러에서 dto로 데이터 받아올 때 @Valid로 검증 진행하는데 articleService.saveArticle() 할 때 한번 더 검증 필요한가
        Long savedId = articleService.saveArticle(memberId, article, request.getUploadFile());

        Article savedArticle = articleService.findById(savedId, false);
        ArticleResDto dto = ArticleResDto.from(savedArticle);

        return Response.success(dto);
    }


    /**
     * 수정 - 작성자 본인만 가능
     */
    @PreAuthorize("@authService.isArticleWriter(#id)")
    @PatchMapping("/articles/{id}")
    @ResponseStatus(OK)
    public Response<ArticleResDto> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleUpdateReqDto request) {
        articleService.update(id, request.getCheckPassword(), request.getTitle(), request.getContent(), request.getUploadFile());
        Article article = articleService.findById(id, false);
        ArticleResDto dto = ArticleResDto.from(article);
        return Response.success(dto);
    }


    /**
     * 삭제 - 작성자 본인
     */
    @PreAuthorize("@authService.isArticleWriter(#id)")
    @DeleteMapping("/articles/{id}")
    @ResponseStatus(OK)
    public Response deleteArticleByUser(@PathVariable Long id, @Valid @RequestBody ArticleDeleteReqDto request) {
        articleService.deleteById(request.getCheckPassword(), id);
        return Response.success();
    }


    /**
     * 삭제 - ADMIN
     */
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/articles/{id}")  //TODO admin용 API를 따로 만들어야 하나
//    @ResponseStatus(OK)
//    public Response deleteArticleByAdmin(@PathVariable Long id) {
//        articleService.deleteById(id);
//        return Response.success();
//    }


    /**
     * 게시글 하나 조회 - 모든 사람 가능
     * 요청 시, 조회한 게시글의 viewCount +1
     */
    //TODO @RequestParam 적용 - sortById, sortByLikes, offset-limit, startDate-endDate. sortByViewCount
    @GetMapping("/articles/{id}")
    @ResponseStatus(OK)
    public Response<ArticleResDto> article(@PathVariable Long id) {
        Article article = articleService.findById(id, true);  //viewCount 증가
        ArticleResDto dto = ArticleResDto.from(article);

        return Response.success(dto);
    }


    /**
     * 게시글 전체 조회 - 모든 사람 가능
     * 요청 시, 조회한 게시글의 viewCount +1
     */
//    @GetMapping("/articles")  //TODO 아래와 요청 동일 GET - "/api/articles"
//    @ResponseStatus(OK)
//    public Response<List<ArticleResDto>> articles() {
//        List<Article> articles = articleService.findAll();
//        List<ArticleResDto> dtoList = articles.stream()
//                .map(a -> {
//                    a.addViewCount();  //viewCount 증가
//                    return a;
//                })
//                .map(a -> new ArticleResDto(a))
//                .collect(Collectors.toList());
//
//        return Response.success(dtoList);
//    }


    /**
     * 게시글 검색
     */
    @GetMapping("/articles")
    @ResponseStatus(OK)
    public Response<Page<ArticleResDto>> searchArticle(ArticleSearchCond cond,
                                                       @RequestParam(required = false, defaultValue = "10") int size,  //한 페이지의 데이터 총 개수
                                                       @RequestParam(required = false, defaultValue = "0") int page,  //현재 페이지
                                                       @RequestParam(required = false, defaultValue = "id") String sort,  //정렬 기준 (viewCount, likes, dislikes, createdDate)
                                                       @RequestParam(required = false, defaultValue = "true") Boolean desc) {  //내림차순 정렬 여부

        //sort 에 가능한 값 : viewCount, likes, dislikes, createdDate
        //TODO sort 관련 아래 검증 필요한가??
        Set<String> sortOptions = new HashSet<>(Arrays.asList("id", "viewcount", "likes", "dislikes", "createdDate"));  //하드코딩...

        if (!sortOptions.contains(sort.toLowerCase())) {
            throw new ApiException(ErrorCode.INVALID_PARAMETER);
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, sort));

        Page<Article> result = articleRepository.searchArticleByCond(cond, pageRequest);

        Page<ArticleResDto> dtoResult = result.map(article -> articleService.toArticleResDto(article.getId()));

        return Response.success(dtoResult);
    }

    /**
     * 게시글의 좋아요 누르는 경우 - 본인글에는 좋아요 못함
     */
    @PatchMapping("/articles/{id}/likes")
    @ResponseStatus(OK)
    public Response likesArticle(@PathVariable Long id, @JwtAuth Member member) {

        Article article = articleService.findById(id, false);

        //본인글인지 확인 -> 본인글이면 예외 터뜨림
        if (isArticleWrittenByMember(article, member)) {
            Response.failure(ErrorCode.SELF_LIKE_ARTICLE.getCode(), "본인 글에는 좋아요가 불가능합니다.");
        }

        return Response.success(articleService.updateLikes(id, member));
    }


    /**
     * 게시글의 싫어요 누르는 경우 - 본인글에는 싫어요 못함
     * 인증 필요
     */
    @PatchMapping("/articles/{id}/dislikes")
    @ResponseStatus(OK)
    public Response dislikesArticle(@PathVariable Long id, @JwtAuth Member member) {

        Article article = articleService.findById(id, false);

        //본인글인지 확인 -> 본인글이면 예외 터뜨림
        if (isArticleWrittenByMember(article, member)) {
            Response.failure(ErrorCode.SELF_DISLIKE_ARTICLE.getCode(), "본인 글에는 싫어요가 불가능합니다.");
        }

        return Response.success(articleService.updateDislikes(id, member));
    }

    private boolean isArticleWrittenByMember(Article article, Member member) {  //TODO 굳이 할 필요 있나.. authService.isArticleWriter(articleId)
        return article.getWriter().equals(member);  //TODO equalsAndHashcode 잘 구현되어있나?
    }

}
