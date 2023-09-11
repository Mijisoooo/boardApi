package practice.board.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import practice.board.domain.Article;
import practice.board.domain.ArticleSearchCond;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static practice.board.domain.QArticle.*;

//@Repository
//@Transactional(readOnly = true)
public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustom {


    @EntityGraph(attributePaths = {"writer"})  //fetch join
    Optional<Article> findById(long id);


    List<Article> findByWriter(Long memberId);




//    /**
//     * 검색 - title, content, nickname
//     */
//    public Map<String, Object> findAllByCond(ArticleSearchCond cond, int size, int page, String sort, Boolean desc) {
//
//        String title = cond.getTitle();
//        String content = cond.getContent();
//        String nickname = cond.getNickname();
//
//        Map<String, Object> resultMap = new HashMap<>();
//
//
//        //전체 데이터 수
//        int totalElements = Math.toIntExact(query.select(article.count())
//                .from(article)
//                .where(likeTitle(title), likeContent(content), likeNickname(nickname))
//                .fetchFirst());
//
//        //전체 페이지 수
//        int totalPages = (int) Math.ceil((double) totalElements / size);
//
//        //현재 페이지의 데이터
//        List<Article> resultData = query.select(article)
//                .from(article)
//                .where(likeTitle(title), likeContent(content), likeNickname(nickname))
//                .offset((long) page * size)
//                .limit(size)
//                .orderBy(sortBy(sort, desc))
//                .fetch();
//
//
//        resultMap.put("size", size);  //페이지 당 데이터 수
//        resultMap.put("numOfElements", resultData.size());  //현재 페이지의 데이터 수
//        resultMap.put("totalElements", totalElements);  //전체 데이터 수
//        resultMap.put("totalPages", totalPages);  //전체 페이지 수
//        resultMap.put("resultData", resultData);  //현재 페이지의 데이터
//
//        return resultMap;
//
//    }
//
//    private OrderSpecifier<?> sortBy(String sort, Boolean desc) {
//
//        OrderSpecifier<?> order = null;
//        Order orderDirection = desc ? Order.DESC : Order.ASC;
//
//        switch (sort.toLowerCase()) {
//            case "id":
//                order = new OrderSpecifier<>(orderDirection, article.id);
//                break;
//            case "viewcount" :
//                order = new OrderSpecifier<>(orderDirection, article.viewCount);
//                break;
//            case "likes" :
//                order = new OrderSpecifier<>(orderDirection, article.likes);
//                break;
//            case "dislikes" :
//                order = new OrderSpecifier<>(orderDirection, article.dislikes);
//                break;
//            case "createddate" :
//                order = new OrderSpecifier<>(orderDirection, article.createdAt);
//                break;
//        }
//
//        return order;
//
//    }
//
//    private BooleanExpression likeTitle(String title) {
//        if (StringUtils.hasText(title)) {
//            return article.title.contains(title);
//        }
//        return null;
//    }
//
//    private BooleanExpression likeContent(String content) {
//        if (StringUtils.hasText(content)) {
//            return article.content.contains(content);
//        }
//        return null;
//    }
//
//    private BooleanExpression likeNickname(String nickname) {
//        if (StringUtils.hasText(nickname)) {
//            return article.writer.nickname.contains(nickname);
//        }
//        return null;
//    }

}
