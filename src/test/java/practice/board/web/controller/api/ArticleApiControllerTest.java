package practice.board.web.controller.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import practice.board.domain.ArticleSearchCond;
import practice.board.service.ArticleService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleApiControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ArticleService articleService;


    private final String SEARCH_URL = "/api/articles";

    @DisplayName("디폴트 검색")
    @Test
    void searchByCond() throws Exception {
        //given
        ArticleSearchCond cond = ArticleSearchCond.builder()
                .nickname("1")
                .title("1")
                .content("1")
                .build();
        String condStr = objectMapper.writeValueAsString(cond);

        //when, then
        mockMvc.perform(get(SEARCH_URL)
                        .content(condStr))
                .andDo(print())
                .andExpect(status().isOk());

    }

}