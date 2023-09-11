package practice.board.web.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Article;
import practice.board.domain.Member;
import practice.board.jwt.JwtService;
import practice.board.repository.MemberRepository;
import practice.board.service.*;
import practice.board.web.dto.comment.CommentSaveReqDto;
import practice.board.web.dto.member.MemberSaveReqDto;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentApiControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CommentService commentService;
    @Autowired ArticleService articleService;
    @Autowired MemberService memberService;
    @Autowired JwtService jwtService;
    @Autowired MemberRepository memberRepository;

    private final String USERNAME = "testUser";
    private final String PASSWORD = "Pass1234!";


    @Test
    @WithMockUser(username = USERNAME, password = PASSWORD)
    void 댓글작성() throws Exception {
        //given
        Member member = Member.createMember(USERNAME, PASSWORD, "email@email.com", "nicky23", null, null);
        member.addUserRole();
        Long memberId = memberRepository.save(member);

        Article article = Article.createArticle("title", "content");
        Long articleId = articleService.saveArticle(memberId, article, Optional.empty());

        CommentSaveReqDto dto = CommentSaveReqDto.builder()
                .articleId(articleId)
                .content("content")
                .build();

        String accessToken = jwtService.createAccessToken(USERNAME);


        //when, then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andDo(print());
    }



    Long signUp() {
        Member member = Member.createMember(USERNAME, PASSWORD, "email@email.com", "nicky2", null, null);
        return memberService.saveMember(MemberSaveReqDto.toDto(member));

        //+ SecurityContext 에 authentication 넣는 것은 @WithMockUser(username = USERNAME, password = PASSWORD) 로 진행
    }


    @Test
    void 모든댓글_조회() throws Exception {
        mockMvc.perform(get("/api/comments"))
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    void 특정게시글의_전체댓글_조회() throws Exception {

        mockMvc.perform(get("/api/articles/1/comments"))
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    void 댓글수정() {
        //given


        //when


        //then

    }


    @Test
    void 댓글삭제() {
        //given


        //when


        //then

    }





}