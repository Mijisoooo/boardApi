package practice.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.board.domain.Address;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.MemberRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import static practice.board.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public Long join(Member member) {

        //중복 여부 검증
        validateDuplicateEmail(member.getEmail());
        validateDuplicateUsername(member.getUsername());
        validateDuplicateNickname(member.getNickname());

        member.encodePassword(passwordEncoder);  //password encoding
        member.addUserRole();  //role 을 USER로 설정 (TODO ADMIN인 경우 따른 방식으로 가입)
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 중복 회원 검증 (email, username, nickname)
     */
    private void validateDuplicateEmail(String email) {
        memberRepository.findByEmail(email).ifPresent((m ->
            {throw new ApiException(DUPLICATE_EMAIL_FOUND, "회원가입 실패 (이미 존재하는 email) email:" + email);}));
    }

    private void validateDuplicateUsername(String username) {
        memberRepository.findByEmail(username).ifPresent((m ->
            {throw new ApiException(DUPLICATE_USERNAME_FOUND, "회원가입 실패 (이미 존재하는 username) username:" + username);}));
    }

    private void validateDuplicateNickname(String nickname) {
        memberRepository.findByEmail(nickname).ifPresent((m ->
            {throw new ApiException(DUPLICATE_NICKNAME_FOUND, "회원가입 실패 (이미 존재하는 nickname) nickname:" + nickname);}));
    }



    /**
     * age, address 조건 없이 수정 가능, nickname(중복 아닌 경우에만 수정 가능), password (조건에 맞는 경우에만 수정 가능)
     */
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
        if (!member.matchPassword(passwordEncoder, checkPassword)) {
            throw new ApiException(WRONG_PASSWORD);
        }

        //nickname 수정
        if (nickname != null && nickname != member.getNickname()) {  //기존 닉네임과 다른 닉네임일 때
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

    /**
     * 삭제 (회원 탈퇴 전에 비밀번호 체크 진행)
     */
    @Transactional
    public void delete(Long id, String checkPassword) {

        //member 조회
        Member member = memberRepository.findById(id).orElseThrow(() ->
                new ApiException(MEMBER_NOT_FOUND, "회원이 존재하지 않습니다. memberId=" + id));

        //비밀번호 일치 여부 확인
        if (!member.matchPassword(passwordEncoder, checkPassword)) {
            throw new ApiException(WRONG_PASSWORD);
        }

        //회원 탈퇴 진행
        memberRepository.delete(member);
    }


    /**
     * 조회
     */
    public List<Member> findAll() {  //TODO 굳이 repository 와 똑같은 메서드인데 서비스단에도 적을 필요 있을까?
        return memberRepository.findAll();
    }


    public Member findById(Long id) {  //TODO 굳이 repository 와 똑같은 메서드인데 서비스단에도 적을 필요 있을까?
        return memberRepository.findById(id).orElse(null);
    }


    /**
     * 로그인
     */
    public boolean login(String username, String password) {
        return memberRepository.login(username, password);
    }
}
