package practice.board.web;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Address;
import practice.board.domain.Article;
import practice.board.domain.Comment;
import practice.board.domain.Member;
import practice.board.service.ArticleService;
import practice.board.service.CommentService;
import practice.board.service.MemberService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//@Component
//@RequiredArgsConstructor
//public class InitDb {
//
//    private final InitService initService;
//
//    @PostConstruct
//    public void init() {
//        initService.dbInit();
//    }
//
//
//    @Component
//    @Transactional
//    @RequiredArgsConstructor
//    static class InitService {
//
//        private final MemberService memberService;
//        private final ArticleService articleService;
//        private final CommentService commentService;
//
//        public void dbInit() {
//
//            //member 저장
//            Address address1 = new Address("city1", "street1", "11111");
//            Member member1 = Member.createMember("test1", "Test123!!", "test1@email.com", "nick1", 20, address1);
//            Address address2 = new Address("city2", "street2", "12111");
//            Member member2 = Member.createMember("test2", "Test456@", "test2@email.com", "nick2", 30, address2);
//            Address address3 = new Address("city3", "street3", "13111");
//            Member member3 = Member.createMember("test3", "Test789@", "test3@email.com", "nick3", 40, address3);
//
//            Long memberId1 = memberService.saveMember(member1);
//            Long memberId2 = memberService.saveMember(member2);
//            Long memberId3 = memberService.saveMember(member3);
//            long[] memberIds = {memberId1, memberId2, memberId3};
//
//            //article 저장
//            List<Long> articleIdList = new ArrayList<>();
//            for (int i = 0; i <= 50; i++) {
//                String title = "게시글" + i;
//                String content = "내용" + i;
//                Article article = Article.createArticle(title, content);
//                int randomIndex = (int) (Math.random() * memberIds.length);
//                Member member = memberService.findById(memberIds[randomIndex]);
//                Long savedId = articleService.saveArticle(member.getId(), article, Optional.empty());
//                articleIdList.add(savedId);
//            }
//
//
//            //comment 저장
//
//            //depth=0 인 댓글 저장
//            List<Long> commentIdList = new ArrayList<>();
//            for (int i = 1; i <= 150; i++) {
//                Article article = articleService.findById(articleIdList.get(i % 50 + 1), false);
//                Member member = memberService.findById(memberIds[i % 3]);
//                String content = "댓글 내용" + i;
//                Long savedId = commentService.saveComment(article.getId(), member.getId(), content, null);
//                commentIdList.add(savedId);
//            }
//
//            //depth >= 1 인 댓글 저장
//            for (int i = 1; i <= 150; i++) {
//                int randomIndex = (int) (Math.random() * memberIds.length);
//                Member member = memberService.findById(memberIds[randomIndex]);
//                String content = "내용" + i;
//                int randomCommentIndex = (int) (Math.random() * (149 + i));
//                Comment parentComment = commentService.findById(commentIdList.get(randomCommentIndex));  //부모댓글 뽑기
//                Long savedId = commentService.saveComment(parentComment.getArticle().getId(), member.getId(), content, parentComment.getId());
//                commentIdList.add(savedId);
//            }
//
//        }
//
//    }
//
//
//}
