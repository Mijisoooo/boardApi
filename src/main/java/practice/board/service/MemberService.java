package practice.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Address;
import practice.board.domain.Member;
import practice.board.domain.Role;
import practice.board.exception.ApiException;
import practice.board.jwt.JwtService;
import practice.board.repository.ArticleRepository;
import practice.board.repository.CommentRepository;
import practice.board.repository.MemberRepository;
import practice.board.web.dto.jwt.TokenDto;
import practice.board.web.dto.member.MemberResDto;
import practice.board.web.dto.member.MemberSaveReqDto;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static practice.board.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    private final String DELETED_MEMBER_USERNAME = "탈퇴한 사용자";

    /**
     * 회원가입
     */
    @Transactional
    public Long saveMember(final MemberSaveReqDto dto) {  //Member 에서 MemberSaveReqDto 로 변경

        //MemberSaveReqDto -> Member 변환
        Member member = MemberSaveReqDto.from(dto);

        //중복 여부 검증
        validateDuplicateMember(member);

        //패스워드 암호화
        member.encodePassword(passwordEncoder);

        //role 을 USER 로 설정 (TODO ADMIN인 경우 따른 방식으로 가입)
        member.addUserRole();

        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 중복 회원 검증 (email, username, nickname)
     */
    private void validateDuplicateMember(Member member) {

        validateDuplicateUsername(member.getUsername());
        validateDuplicateEmail(member.getEmail());
        validateDuplicateNickname(member.getNickname());

    }

    private void validateDuplicateUsername(String username) {
        memberRepository.findByUsername(username).ifPresent((m ->
        {throw new ApiException(DUPLICATE_USERNAME_FOUND, "회원가입 실패 (이미 존재하는 username) username:" + username);}));
    }

    private void validateDuplicateEmail(String email) {
        memberRepository.findByEmail(email).ifPresent((m ->
        {throw new ApiException(DUPLICATE_USERNAME_FOUND, "회원가입 실패 (이미 존재하는 email) email:" + email);}));
    }

    private void validateDuplicateNickname(String nickname) {
        memberRepository.findByNickname(nickname).ifPresent((m ->
        {throw new ApiException(DUPLICATE_NICKNAME_FOUND, "회원가입 실패 (이미 존재하는 nickname) nickname:" + nickname);}));
    }


    @Transactional
    public void updateRefreshToken(Long memberId, String refreshToken) {
        Member member = findById(memberId);
        member.updateRefreshToken(refreshToken);
    }


    /**
     * age, address 조건 없이 수정 가능, nickname(중복 아닌 경우에만 수정 가능), password (조건에 맞는 경우에만 수정 가능)
     */
    //TODO 만약 username을 파라미터로 던져주는 경우는 어떤 예외를 던져줘야할까?
    //TODO 기존 회원 정보와 변함이 없는 경우에는 "기존 정보와 동일함" 메세지를 던져줄까?
    @Transactional
    public void update(Long id, String checkPassword, String newPassword, String nickname, Integer age, Address address) {

        /*
        //로그인한 아이디(username)로 회원 찾기  //TODO service layer에서 로그인한 아이디를 가져오면 안될 듯... 컨트롤러에서 찾고 id를 서비스단으로 넘겨주는 건 어떤가?
        String loginUsername = SecurityUtil.getLoginUsername();
        memberRepository.findByUsername(loginUsername).orElseThrow(() ->
                new NoSuchElementException("해당 회원이 없습니다. username:" + loginUsername));
        */

        //member 조회 by id
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApiException(MEMBER_NOT_FOUND));

        //정보 수정 전, 비밀번호 일치 여부 확인
        if (!member.validatePassword(passwordEncoder, checkPassword)) {
            throw new ApiException(WRONG_PASSWORD);
        }

        //nickname 수정
        if (nickname != null && !nickname.equals(member.getNickname())) {  //기존 닉네임과 다른 닉네임일 때
            validateDuplicateNickname(nickname);
            member.updateNickname(nickname);
        }

        //age 수정
        if (age != null) {
            member.updateAge(age);
        }

        //address 수정
        if (address != null) {
            member.updateAddress(address);
        }

        //password 수정
        if (validatePassword(newPassword)) {
            member.updatePassword(passwordEncoder, newPassword);
        }
        else if (newPassword != null) { //newPassword 가 형식에 맞지 않는 경우
            throw new ApiException(BAD_REQUEST);
        }

        memberRepository.save(member);
    }

    /**
     * password 가 형식에 맞는지 검증
     */
    private boolean validatePassword(String newPassword) {  //TODO controller side에서 validation 진행하지만 Service layer 에서도 한번 더 validation 진행
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[._?!*])[a-zA-Z\\d._?!*]{8,20}$";
        Pattern passwordPattern = Pattern.compile(passwordRegex);

        return newPassword != null && passwordPattern.matcher(newPassword).matches();
    }


//    /**
//     * 삭제 (회원 탈퇴 전에 비밀번호 체크 진행, ADMIN 인 경우 체크 안함)
//     */
//    @Transactional
//    public void delete(Long id, String checkPassword) {
//
//        //member 조회
//        Member member = findById(id);
//
//        //비밀번호 일치 여부 확인
//        if (!member.validatePassword(passwordEncoder, checkPassword) && !Role.ADMIN.equals(member.getRole())) {
//            throw new ApiException(WRONG_PASSWORD);
//        }
//
//        //회원 탈퇴 진행
//        memberRepository.delete(member);
//    }


    /**
     * 삭제 (회원 탈퇴 전에 비밀번호 체크 진행, ADMIN 인 경우 체크 안함)
     * Member 탈퇴해도 작성한 글, 댓글은 남아있도록 하기 위해서
     */
    @Transactional
    public void deleteMemberWithHistory(Long id, String checkPassword) {

        //member 조회
        Member memberToDelete = findById(id);
        Member deletedMember = getOrCreateDeletedMember();

        //비밀번호 일치 여부 확인
        if (!memberToDelete.validatePassword(passwordEncoder, checkPassword) && !Role.ADMIN.equals(memberToDelete.getRole())) {
            throw new ApiException(WRONG_PASSWORD);
        }

        //작성 글, 댓글의 member 이름 변경
        articleRepository.findByWriter(id).forEach(article -> {
            article.setWriter(deletedMember);
            articleRepository.save(article);
        });

        commentRepository.findByWriter(id).forEach(comment -> {
            comment.setWriter(deletedMember);
            commentRepository.save(comment);
        });

        //member 삭제
        memberRepository.deleteById(id);
    }

    private Member getOrCreateDeletedMember() {
        Optional<Member> deletedMemberOptional = memberRepository.findByUsername(DELETED_MEMBER_USERNAME);

        if (deletedMemberOptional.isPresent()) {
            return deletedMemberOptional.get();
        }
        else {
            Member deletedMember = Member.createDeletedMember();
            return memberRepository.save(deletedMember);
        }
    }



    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() ->
                        new ApiException(MEMBER_NOT_FOUND, "회원이 존재하지 않습니다. memberId=" + id));
    }


    /**
     * 로그인
     * jwt 발급, Authentication 객체 생성해서 SecurityContext 에 저장
     * @return 로그인한 member 의 id 값 반환
     */
    @Transactional
    public TokenDto login(String username, String password) {

        //member 조회  TODO 중복이 많다.. jwtService.createAccessToken() -> cuastomUserDetailsService.loadUserByUesrname()에서도 검사하는데
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> {
            throw new ApiException(LOGIN_FAILURE);
        });

        //password 일치하는지 검증
        if (!member.validatePassword(passwordEncoder, password)) {
            throw new ApiException(LOGIN_FAILURE);
        }

        //1. JWT 발급
        //access token 생성
        String accessToken = jwtService.createAccessToken(member.getUsername());
        //refresh token 생성
        String refreshToken = jwtService.createRefreshToken();
        //db에 refresh token 저장
        member.updateRefreshToken(refreshToken);

        //2. Authentication 객체 생성 후 SecurityContext 에 저장
        jwtService.saveAuthentication(accessToken);

        //TODO 응답 헤더로 access token, refresh token 보내기 ??
//        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        //TokenDto 생성해서 리턴
        TokenDto tokenDto = TokenDto.builder()
                .memberId(member.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return tokenDto;
    }

    /**
     * MemberResDto 로 변환
     */
    public MemberResDto toMemberResDto(Long memberId) {
        Member member = findById(memberId);
        return MemberResDto.from(member);
    }

}
