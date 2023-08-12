package practice.board.web.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import practice.board.domain.Member;
import practice.board.jwt.JwtService;
import practice.board.repository.MemberRepository;
import practice.board.response.Response;
import practice.board.service.MemberService;
import practice.board.web.dto.member.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberApiController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    //TODO 반환타입이 Response 라고만 적혀있으니까 그 안에 어떤 데이터가 담겨나가는지 모르겠네 -> Response<MemberResDto> 요렇게 쓸 수 있도록 바꾸자

    /**
     * 회원 저장
     */
    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<MemberResDto> saveMember(@Valid @RequestBody MemberSaveReqDto request) {
        Member member = MemberSaveReqDto.from(request);
        Long savedId = memberService.saveMember(member);
        Member savedMember = memberService.findById(savedId);
        MemberResDto dto = new MemberResDto(savedMember);
        return Response.success(dto);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Response<MemberLoginResDto> login(@Valid @RequestBody final MemberLoginReqDto request) {

        Long loginMemberId = memberService.login(request.getUsername(), request.getPassword());
        Member member = memberRepository.findById(loginMemberId).get();

        String accessToken = jwtService.createAccessToken(member.getUsername());  //access token 생성
        String refreshToken = jwtService.createRefreshToken();  //refresh token 생성
        member.updateRefreshToken(refreshToken);  //db에 refresh token 저장

        MemberLoginResDto dto = MemberLoginResDto.builder()
                .id(loginMemberId)
                .username(request.getUsername())
                .nickname(member.getNickname())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return Response.success(dto);
    }


    /**
     * 전체 회원 조회 - ADMIN 가능
     */
    @GetMapping("/members")
    @ResponseStatus(HttpStatus.OK)
    public Response<List<MemberResDto>> members() {
        List<Member> members = memberService.findAll();
        List<MemberResDto> dtos = members.stream()
                .map(MemberResDto::new)
                .collect(Collectors.toList());

        return Response.success(dtos);
    }

    /**
     * 회원 조회 by id - ADMIN 가능
     */
    @GetMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response<MemberResDto> member(@PathVariable Long id) {
        Member member = memberService.findById(id);
        MemberResDto dto = new MemberResDto(member);
        return Response.success(dto);
    }

    /**
     * 회원정보 수정 (nickname, age, address, password) - 본인만 가능
     */
    @PreAuthorize("@AuthService.hasId(#id)")  //본인만 가능
    @PatchMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response<MemberResDto> updateMember(@PathVariable Long id, @Valid @RequestBody MemberUpdateReqDto request) {
        memberService.update(id, request.getCheckPassword(), request.getNewPassword(), request.getNickname(), request.getAge(), request.getAddress());
        Member member = memberService.findById(id);
        MemberResDto dto = new MemberResDto(member);
        return Response.success(dto);
    }


    /**
     * 회원 탈퇴 - 본인 or ADMIN 가능
     */
    @DeleteMapping("/members/{id}")
    @PreAuthorize("hasRole('ADMIN') or @AuthService.hasId(#id)")  //ADMIN or 본인 가능
    @ResponseStatus(HttpStatus.OK)
    public Response deleteMember(@PathVariable Long id, @Valid @RequestBody MemberDeleteDto request) {
        memberService.delete(id, request.getPassword());
        return Response.success();
    }

}
