package practice.board.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Article;
import practice.board.domain.ArticleSearchCond;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

//@DataJpaTest  //Spring Data JPA repository 로 바꾸면 사용 가능
                //or articleRepository, 여기에 주입되는 빈(em, JPAQueryFactory)을 @Bean으로 등록하는 설정파일을 넣어주면 되는데 이건 너무 복잡
@SpringBootTest
@Transactional
@Slf4j
class ArticleRepositoryTest {

    @Autowired private ArticleRepository articleRepository;

    @Autowired private MemberRepository memberRepository;


    //TODO @BeforeEach 에서 만든 변수를 @Test 에서 사용할 수 있는 다른 방법 없을까???
//    private Member member1;
//    private Member member2;
//    private Article article1;
//    private Article article2;
//    private Article article3;
//    private Article article4;
//    private Article article5;
//    private Article article6;
//    private Article article7;
//    private Article article8;
//    private Article article9;
//    private Article article10;


    private ArticleSearchCond setArticleSearchCond(String title, String content, String nickname) {
        return ArticleSearchCond.builder()
                .title(title)
                .content(content)
                .nickname(nickname)
                .build();
    }

//    @BeforeEach
//    void beforeEach() {
//        Member member1 = Member.createMember("username", "Pass1234!", "test@email.com", "nick", 20, null);
//        member1.addUserRole();
//
//        Member member2 = Member.createMember("username2", "Pass1234!", "test2@email.com", "nick2", 20, null);
//        member2.addUserRole();
//
//        Long memberId1 = memberRepository.save(member1);
//        Long memberId2 = memberRepository.save(member2);
//
//        Member foundMember1 = memberRepository.findById(memberId1).get();
//        Member foundMember2 = memberRepository.findById(memberId2).get();
//
//
//        article1 = Article.createArticle(foundMember1, "title1", "content1");
//        article2 = Article.createArticle(foundMember1, "title2", "content2");
//        article3 = Article.createArticle(foundMember1, "title3", "content3");
//        article4 = Article.createArticle(foundMember1, "title4", "content4");
//        article5 = Article.createArticle(foundMember1, "title5", "content5");
//        article6 = Article.createArticle(foundMember1, "title6", "content6");
//        article7 = Article.createArticle(foundMember2, "title7", "content7");
//        article8 = Article.createArticle(foundMember2, "title8", "content8");
//        article9 = Article.createArticle(foundMember2, "title9", "content9");
//        article10 = Article.createArticle(foundMember2, "title10", "content10");
//
//
//        articleRepository.save(article1);
//        articleRepository.save(article2);
//        articleRepository.save(article3);
//        articleRepository.save(article4);
//        articleRepository.save(article5);
//        articleRepository.save(article6);
//        articleRepository.save(article7);
//        articleRepository.save(article8);
//        articleRepository.save(article9);
//        articleRepository.save(article10);
//    }


    @DisplayName("검색조건으로 검색 테스트")
    @Test
    void findAllByCond_test() {
        //given - data.sql 로 데이터 입력

        //when
        ArticleSearchCond cond1 = setArticleSearchCond("title", "content", "nick");  //조회결과 : 10건
        Map<String, Object> resultMap1 = articleRepository.findAllByCond(cond1, 5, 0, "id", true);
        Map<String, Object> resultMap2 = articleRepository.findAllByCond(cond1, 5, 1, "id", true);
        Map<String, Object> resultMap3 = articleRepository.findAllByCond(cond1, 3, 1, "id", true);

        ArticleSearchCond cond2 = setArticleSearchCond("tle1", "content", "nick");  //조회결과 : 2건
        Map<String, Object> resultMap4 = articleRepository.findAllByCond(cond2, 3, 0, "id", false);

        ArticleSearchCond cond3 = setArticleSearchCond("tle1", "content", "nick2");  //조회결과 : 0건
        Map<String, Object> resultMap5 = articleRepository.findAllByCond(cond3, 3, 0, "id", false);

        List<Article> resultList1 = (List<Article>) resultMap1.get("resultData");
        List<Article> resultList2 = (List<Article>) resultMap2.get("resultData");
        List<Article> resultList3 = (List<Article>) resultMap3.get("resultData");
        List<Article> resultList4 = (List<Article>) resultMap4.get("resultData");
        List<Article> resultList5 = (List<Article>) resultMap5.get("resultData");


        //then
        assertThat(resultMap1.get("numOfElements")).isEqualTo(resultList1.size());
        assertThat(resultMap1.get("totalElements")).isEqualTo(10);
        assertThat(resultMap1.get("totalPages")).isEqualTo(2);
//        assertThat(resultList1).contains(article10);
//        assertThat(resultList1).contains(article9);
//        assertThat(resultList1).contains(article8);
//        assertThat(resultList1).contains(article7);
//        assertThat(resultList1).contains(article6);

        assertThat(resultMap2.get("numOfElements")).isEqualTo(resultList2.size());
//        assertThat(resultList2).contains(article5);
//        assertThat(resultList2).contains(article4);
//        assertThat(resultList2).contains(article3);
//        assertThat(resultList2).contains(article2);
//        assertThat(resultList2).contains(article1);

        assertThat(resultMap3.get("numOfElements")).isEqualTo(resultList3.size());
        assertThat(resultMap3.get("totalPages")).isEqualTo(4);
//        assertThat(resultList3).contains(article7);
//        assertThat(resultList3).contains(article6);
//        assertThat(resultList3).contains(article5);

        assertThat(resultMap4.get("numOfElements")).isEqualTo(resultList4.size());
        assertThat(resultMap4.get("totalElements")).isEqualTo(2);
//        assertThat(resultList4).contains(article1);
//        assertThat(resultList4).contains(article10);

        assertThat(resultMap5.get("numOfElements")).isEqualTo(resultList5.size());
        assertThat(resultMap5.get("totalElements")).isEqualTo(0);
//        assertThat(resultList5).contains(article10);

    }






}