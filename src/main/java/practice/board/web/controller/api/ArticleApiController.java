package practice.board.web.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import practice.board.domain.Article;
import practice.board.response.Response;
import practice.board.service.ArticleService;
import practice.board.web.dto.article.ArticleResDto;
import practice.board.web.dto.article.ArticleSaveReqDto;
import practice.board.web.dto.article.ArticleUpdateReqDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArticleApiController {

    private final ArticleService articleService;

    /**
     * 조회
     */
    @GetMapping("/articles")
    public Response articles() {
        List<Article> articles = articleService.findAll();
        List<ArticleResDto> dtos = articles.stream()
                .map(a -> new ArticleResDto(a))  //TODO 이 컨트롤러 다 생성자로 conversion 하는데,, Member의 방식이랑 동일하도록 변경할 것
                .collect(Collectors.toList());

        return Response.success(dtos);
    }

    /**
     * 저장
     */
    @PostMapping("/articles")
    public Response saveArticle(@RequestBody ArticleSaveReqDto request) {
        //TODO loginMember 만 글 작성 가능
        Long savedId = articleService.save(request.getWriterId(), request.getTitle(), request.getContent());
        Article article = articleService.findById(savedId, false);
        ArticleResDto dto = new ArticleResDto(article);
        return Response.success(dto);
    }

    /**
     * 수정
     */
    @PatchMapping("/articles/{id}")
    public Response updateArticle(@PathVariable Long id, @RequestBody ArticleUpdateReqDto request) {
        articleService.update(id, request.getTitle(), request.getContent(), request.getFilePath());
        Article article = articleService.findById(id, false);
        ArticleResDto dto = new ArticleResDto(article);
        return Response.success(dto);
    }

    /**
     * 삭제
     */
    @DeleteMapping("/articles/{id}")
    public Response deleteArticle(@PathVariable Long id) {
        articleService.deleteById(id);
        return Response.success();
    }


}
