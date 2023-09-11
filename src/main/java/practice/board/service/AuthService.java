package practice.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import practice.board.domain.Article;
import practice.board.domain.Comment;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.ArticleRepository;
import practice.board.repository.CommentRepository;
import practice.board.repository.MemberRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    public static String getLoginUsername() {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUsername();
    }

    /**
     * 로그인한 member 의 id 가져오기
     */
    public Long getLoginMemberId() {
        Optional<Member> member = memberRepository.findByUsername(getLoginUsername());
        return member.orElseThrow(() ->
                new ApiException(ErrorCode.MEMBER_NOT_FOUND))
                .getId();

    }

    /**
     * 로그인한 member 의 id가 파라미터의 memberId 와 일치하는지 체크
     */
    public boolean hasId(Long memberId) {
        Optional<Member> optionalMember = memberRepository.findByUsername(getLoginUsername());
        return optionalMember.map(member -> member.getId().equals(memberId)).orElse(false);
    }

    /**
     * 로그인한 member 가 파라미터(articleId)에 해당하는 article 의 작성자인지 체크
     */
    public boolean isArticleWriter(Long articleId) {
        Article article = articleRepository.findById(articleId).get();  //article이 확실히 존재하기에 get() 사용
        Optional<Member> optionalMember = memberRepository.findByUsername(getLoginUsername());

        return optionalMember.map(member ->
                        member.getArticleList().contains(article))
                .orElse(false);
    }

    /**
     * 로그인한 member 가 파라미터(commentId)에 해당하는 comment 의 작성자인지 체크
     */
    public boolean isCommentWriter(Long commentId) {
        Comment comment = commentRepository.findById(commentId).get();  //comment가 확실히 존재하기에 get() 사용
        Optional<Member> optionalMember = memberRepository.findByUsername(getLoginUsername());

        return optionalMember.map(member ->
                        member.getCommentList().contains(comment))
                .orElse(false);
    }




}
