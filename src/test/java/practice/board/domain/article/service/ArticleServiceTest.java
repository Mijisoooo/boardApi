package practice.board.domain.article.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import practice.board.domain.Article;
import practice.board.domain.Member;
import practice.board.service.ArticleService;
import practice.board.service.MemberService;
import practice.board.web.dto.member.MemberSaveReqDto;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@Transactional
class ArticleServiceTest {

    @Autowired private ArticleService articleService;
    @Autowired private MemberService memberService;
    @Autowired EntityManager em;

    private String PASSWORD = "password";

    private Member saveMember() {
        Member member = Member.createMember("username", "password123!", "email@email.com", "nick", 20, null);
        memberService.saveMember(MemberSaveReqDto.toDto(member));
        return member;
    }


    @DisplayName("게시글 저장 성공_첨부파일_X")
    @Test
    void save_success_without_file() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", "content");

        //whens
        articleService.saveArticle(member.getId(), article, Optional.empty());

        //then
        assertThat(article).isNotNull();
        assertThat(article.getFilePath()).isNull();
    }


    @DisplayName("게시글 저장 성공_첨부파일_O")
    @Test
    void save_success_with_file() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", "content");

        MultipartFile file = new MockMultipartFile("fileName", "file-content".getBytes());
        //when
        articleService.saveArticle(member.getId(), article, Optional.of(file));

        //then
        assertThat(article).isNotNull();
        assertThat(article.getFilePath()).isNotNull();
    }


    @DisplayName("게시글 저장 실패_제목 없음")
    @Test
    void save_fail_no_title() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle(null, "content");

        //when, then
//        assertThatThrownBy(() -> articleService.saveArticle(member.getId(), article, Optional.empty()))
//                .isInstanceOf(Exception.class);
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());
        assertThrows(Exception.class, () -> em.flush());
        em.clear();
        //db에 저장될 때 오류 발생
        //컨트롤러에서 dto로 받아올 때 검증 진행하고, 이후에는 따로 검증 진행 안하기 때문에

        Article savedArticle = articleService.findById(articleId, false);
        assertThat(savedArticle).isNull();

    }


    @DisplayName("게시글 저장 실패_내용 없음")
    @Test
    void save_fail_no_content() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", null);

        //when, then
//        assertThatThrownBy(() -> articleService.saveArticle(member.getId(), article, Optional.empty()))
//                .isInstanceOf(Exception.class);
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());
        assertThrows(Exception.class, () -> em.flush());
        em.clear();
        //db에 저장될 때 오류 발생
        //컨트롤러에서 dto로 받아올 때 검증 진행하고, 이후에는 따로 검증 진행 안하기 때문에

        Article savedArticle = articleService.findById(articleId, false);
        assertThat(savedArticle).isNull();
    }


    @DisplayName("게시글 수정 성공")
    @Test
    void update_success() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", "content");
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());
        MultipartFile newFile = new MockMultipartFile("filename", "This is it".getBytes());

        //when
        articleService.update(articleId, PASSWORD, null, "new-content", newFile);

        //then
        assertThat(article.getContent()).isEqualTo("new-content");
        assertThat(article.getTitle()).isEqualTo("title");
        assertThat(article.getFilePath()).isNotNull();
    }


    @DisplayName("게시글 수정 실패_패스워드 틀림")
    @Test
    void update_fail_wrong_checkPassword() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", "content");
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());

        //when, then
        assertThrows(Exception.class, () ->
                articleService.update(articleId, PASSWORD+1, null, "new-content", null));

        assertThat(article.getContent()).isNotEqualTo("new-content");
    }


    @DisplayName("게시글 삭제 성공")
    @Test
    void delete_success() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", "content");
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());

        //when
        articleService.deleteById(PASSWORD, articleId);
        em.flush();
        em.clear();

        //then
        assertThat(articleService.findById(articleId, false)).isNull();
    }


    @DisplayName("게시글 삭제 성공_ADMIN")
    @Test
    void delete_success_ADMIN() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", "content");
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());

        //when
        articleService.deleteById(articleId);
        em.flush();
        em.clear();

        //then
        assertThat(articleService.findById(articleId, false)).isNull();
    }


    @DisplayName("게시글 삭제 실패_checkPassword 틀림")
    @Test
    void delete_fail_wrong_checkPassword() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", "content");
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());

        //when, then
        assertThrows(Exception.class, () -> articleService.deleteById(PASSWORD+1, articleId));
        assertThat(articleService.findById(articleId, false)).isNotNull();
    }


    @DisplayName("게시글 조회수 증가 성공")
    @Test
    void addViewCount() {
        //given
        Member member = saveMember();
        Article article = Article.createArticle("title", "content");
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());
        Article savedArticle = articleService.findById(articleId, false);

        //when
        articleService.findById(articleId, true);  //viewCount + 1

        //then
        assertThat(savedArticle.getViewCount()).isEqualTo(1);
    }

    @DisplayName("게시글 조회_쿼리 확인")
    @Test
    void findById() {
        //given
        Member member = saveMember();
        log.info("===============1===============");
        Article article = Article.createArticle("title", "content");
        log.info("===============2===============");
        Long articleId = articleService.saveArticle(member.getId(), article, Optional.empty());
        log.info("===============3===============");

        //when, then
        Article savedArticle = articleService.findById(articleId, false);
        log.info("savedArticle.getWriter().getNickName()={}, getTitle()={}, getContent()={}",
                savedArticle.getWriter().getNickname(), savedArticle.getTitle(), savedArticle.getContent());

    }



}