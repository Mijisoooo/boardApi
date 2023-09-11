package practice.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import practice.board.domain.*;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.ArticleRepository;
import practice.board.repository.DislikeArticleRepository;
import practice.board.repository.LikeArticleRepository;
import practice.board.repository.MemberRepository;
import practice.board.service.file.LocalFileService;
import practice.board.web.dto.article.ArticleResDto;

import java.util.List;
import java.util.Optional;

import static practice.board.exception.ErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocalFileService localFileService;
    private final DislikeArticleRepository dislikeArticleRepository;
    private final LikeArticleRepository likeArticleRepository;

    private static final String SUCCESS_LIKE_ARTICLE = "좋아요 처리 완료";
    private static final String SUCCESS_UNLIKE_ARTICLE = "좋아요 취소 완료";
    private static final String SUCCESS_DISLIKE_ARTICLE = "싫어요 처리 완료";
    private static final String SUCCESS_UNDISLIKE_ARTICLE = "싫어요 취소 완료";


    /**
     * 저장
     */
    @Transactional
    public Long saveArticle(Long memberId, Article article, Optional<MultipartFile> uploadFile) {  //TODO Optional로 받는게 불편한데...

        //member 찾아서 article 의 writer 로 저장  //TODO Article.createArticle() 로 생성할 때 이미 Member 넣어줬는데,,,
        memberRepository.findById(memberId).ifPresentOrElse(article::setWriter, () -> {
            throw new ApiException(MEMBER_NOT_FOUND, "해당 회원이 없습니다. memberId=" + memberId);
        });

        //article 의 file 저장
        uploadFile.ifPresent(file ->
                article.updateFilePath(localFileService.save(file)));

        Article savedArticle = articleRepository.save(article);
        return savedArticle.getId();
    }

    /**
     * 수정 (작성자 본인의 password 일치하면 수정 진행)
     */
    @Transactional
    public void update(Long id, String checkPassword, String newTitle, String newContent, MultipartFile newUploadFile) {
        //TODO 파라미터를 다 Optional 로 받아올까? or 그냥 이대로 받아오고 아래 코드에서 Optional.ofNullable()를 추가하면 되지 않나?

        //Article, Member 조회
        Article article = findById(id, false);
        Member member = article.getWriter();

        //게시글 수정 전, 작성자의 비밀번호 일치 여부 확인
        if (!member.validatePassword(passwordEncoder, checkPassword)) {
            throw new ApiException(WRONG_PASSWORD);
        }

        //title 수정
        Optional.ofNullable(newTitle)
                .ifPresent(article::updateTitle);

        //content 수정
        Optional.ofNullable(newContent)
                .ifPresent(article::updateContent);

        //filePath 수정
        Optional.ofNullable(newUploadFile)
                .ifPresent(file ->
                        article.updateFilePath(localFileService.save(file)));

        articleRepository.save(article);
    }

    /**
     * 삭제 (ADMIN이 아닌 경우, 작성자가 삭제 -> 작성자의 비밀번호 입력)
     */
    @Transactional
    public void deleteById(String checkPassword, Long id) {
        Article article = findById(id, false);
        Member member = article.getWriter();

        //게시글 삭제 전, 작성자의 비밀번호 일치 여부 확인, ADMIN 인 경우 체크 안함
        if (!member.validatePassword(passwordEncoder, checkPassword) && !Role.ADMIN.equals(member.getRole())) {
            throw new ApiException(WRONG_PASSWORD);
        }

        articleRepository.deleteById(id);
    }

    /**
     * 삭제 - ADMIN이 삭제할때 사용하는 메서드 (비밀번호 체크 없음)
     */
    @Transactional
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }


    /**
     * id로 조회
     * @param increaseViewCount 조회수 1 올릴지 여부
     */
    public Article findById(Long id, Boolean increaseViewCount) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new ApiException(ARTICLE_NOT_FOUND));

        if (increaseViewCount) {
            article.increaseViewCount();
        }

        return article;
    }


    /**
     * 좋아요 관련
     */
    @Transactional
    public String updateLikes(Long id, Member member) {
        Article article = findById(id, false);

        //이미 좋아요 했던 글인데, 좋아요 누르는 경우 -> 좋아요 취소
        if (hasLikedArticle(article, member)) {
            return unlikeArticle(article, member);
        }

        //좋아요 한 적 없으면 -> 좋아요 처리
        return likeArticle(article, member);
    }


    /**
     * 좋아요 처리
     */
    @Transactional
    public String likeArticle(Article article, Member member) {
        article.increaseLikes();
        LikeArticle likeArticle = LikeArticle.create(article, member);
        likeArticleRepository.save(likeArticle);

        return SUCCESS_LIKE_ARTICLE;
    }

    /**
     * 좋아요 취소 처리
     */
    @Transactional
    public String unlikeArticle(Article article, Member member) {
        article.decreaseLikes();
        likeArticleRepository.findByArticleAndMember(article, member)
                .ifPresentOrElse(likeArticleRepository::delete, () ->
                        new ApiException(ErrorCode.LIKEARTICLE_NOT_FOUND));
        return SUCCESS_UNLIKE_ARTICLE;
    }

    /**
     * 좋아요 한 글인지 체크
     */
    public boolean hasLikedArticle(Article article, Member member) {
        return likeArticleRepository.findByArticleAndMember(article, member).isPresent();
    }


    /**
     * 싫어요 관련
     */
    public String updateDislikes(Long id, Member member) {
        Article article = findById(id, false);

        if (hasDislikedArticle(article, member)) {
            return unDislikeArticle(article, member);
        }

        return dislikeArticle(article, member);
    }

    /**
     * 싫어요 처리
     */
    @Transactional
    public String dislikeArticle(Article article, Member member) {
        article.increaseDislikes();
        DislikeArticle dislikeArticle = DislikeArticle.create(article, member);
        dislikeArticleRepository.save(dislikeArticle);

        return SUCCESS_DISLIKE_ARTICLE;
    }

    /**
     * 싫어요 취소 처리
     */
    @Transactional
    public String unDislikeArticle(Article article, Member member) {
        article.decreaseDislikes();
        dislikeArticleRepository.findByArticleAndMember(article, member)
                .ifPresentOrElse(dislikeArticleRepository::delete, () ->
                        new ApiException(ErrorCode.DISLIKEARTICLE_NOT_FOUND));

        return SUCCESS_UNDISLIKE_ARTICLE;
    }

    /**
     * 싫어요 한 글인지 체크
     */
    public boolean hasDislikedArticle(Article article, Member member) {
        return dislikeArticleRepository.findByArticleAndMember(article, member).isPresent();
    }

    public ArticleResDto toArticleResDto(long articleId) {
        Article article = findById(articleId, false);
        return ArticleResDto.from(article);
    }
}
