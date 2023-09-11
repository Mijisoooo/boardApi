package practice.board.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import practice.board.domain.Article;
import practice.board.domain.ArticleSearchCond;

import java.util.List;

import static practice.board.domain.QArticle.*;

@RequiredArgsConstructor
public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Article> searchArticleByCond(ArticleSearchCond cond, Pageable pageable) {  //TODO queryDsl 페이징 적용 필요
        //content를 가져오는 쿼리와 count 쿼리 분리하고 PageableExecutionUtils 사용하여 응답

        String title = cond.getTitle();
        String content = cond.getContent();
        String nickname = cond.getNickname();

        JPAQuery<Article> query = queryFactory.select(article)
                .from(article)
                .where(
                        likeTitle(title),
                        likeContent(content),
                        likeNickname(nickname)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        //정렬 조건 추가
        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(article.getType(), article.getMetadata());
            query.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC,
                    pathBuilder.get(o.getProperty())));
        }

        List<Article> resultContent = query.fetch();

        //count 가져오는 쿼리
        JPAQuery<Long> countQuery = queryFactory.select(article.count())
                .from(article)
                .where(
                        likeTitle(title),
                        likeContent(content),
                        likeNickname(nickname)
                );

        return PageableExecutionUtils.getPage(resultContent, pageable, countQuery::fetchOne);
        //getPage() 메서드 내부에서 해당 람다식을 호출하기 전까지 count 쿼리 실행 X - 필요할 때까지 지연! (항상 total count 를 계산하지 않음)
        //(PageImpl : 쿼리 항상 즉시 두방 나감 - content, count)

    }

        private BooleanExpression likeTitle(String title) {
        if (StringUtils.hasText(title)) {
            return article.title.contains(title);
        }
        return null;
    }

    private BooleanExpression likeContent(String content) {
        if (StringUtils.hasText(content)) {
            return article.content.contains(content);
        }
        return null;
    }

    private BooleanExpression likeNickname(String nickname) {
        if (StringUtils.hasText(nickname)) {
            return article.writer.nickname.contains(nickname);
        }
        return null;
    }
}
