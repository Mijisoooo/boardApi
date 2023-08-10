package practice.board.web.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import practice.board.domain.Member;
import practice.board.exception.ApiException;
import practice.board.exception.ErrorCode;
import practice.board.repository.MemberRepository;
import practice.board.service.MemberService;
import practice.board.web.dto.member.MemberLoginResDto;
import practice.board.web.dto.member.MemberLoginReqDto;

import static practice.board.exception.ErrorCode.*;

@RestController
@RequiredArgsConstructor
public class LoginApiController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody MemberLoginReqDto request) {
        boolean isPresent = memberService.login(request.getUsername(), request.getPassword());
        if (isPresent) {
            Member loginMember = memberRepository.findByUsername(request.getUsername()).get();

            MemberLoginResDto loginResDto = MemberLoginResDto.builder()
                    .id(loginMember.getId())
                    .username(loginMember.getUsername())
                    .build();
            return new ResponseEntity(loginResDto, HttpStatus.OK);
        }
        else if (!memberRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ApiException(LOGIN_FAILURE, "password가 틀립니다. username=" + request.getUsername());
        }
        else {
            throw new ApiException(MEMBER_NOT_FOUND, "해당 username으로 가입된 회원이 없습니다. username=" + request.getUsername());
        }
    }
}
