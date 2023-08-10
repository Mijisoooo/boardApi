package practice.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Article;
import practice.board.domain.Member;
import practice.board.repository.ArticleRepository;
import practice.board.repository.MemberRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    /**
     * 저장
     */
    @Transactional
    public Long save(Long memberId, String title, String content) {
        Member member = memberRepository.findById(memberId).orElseThrow(() ->
                new NoSuchElementException("해당 회원이 없습니다. memberId=" + memberId));
        Article article = Article.createArticle(member, title, content);
        return articleRepository.save(article);
    }

    /**
     * 수정
     */
    @Transactional
    public void update(Long id, String title, String content, String filePath) {
        articleRepository.update(id, title, content, filePath);
    }

    /**
     * 삭제
     */
    @Transactional
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }


    /**
     * id로 조회
     * @param addViewCount 조회수 1 올릴지 여부
     */
    public Article findById(Long id, Boolean addViewCount) {
        Article article = articleRepository.findById(id);
        if (addViewCount) {
            article.addViewCount();
        }
        return article;
    }

    public List<Article> findAll() {
        return articleRepository.findAll();
    }
}
