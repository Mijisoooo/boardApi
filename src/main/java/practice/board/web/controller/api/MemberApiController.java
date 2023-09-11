package practice.board.web.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import practice.board.aop.Trace;
import practice.board.domain.Member;
import practice.board.jwt.JwtService;
import practice.board.repository.MemberRepository;
import practice.board.response.Response;
import practice.board.service.MemberService;
import practice.board.web.dto.jwt.TokenDto;
import practice.board.web.dto.member.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class MemberApiController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    //TODO 반환타입이 Response 라고만 적혀있으니까 그 안에 어떤 데이터가 담겨나가는지 모르겠네 -> Response<MemberResDto> 요렇게 쓸 수 있도록 바꾸자

    /**
     * 회원 저장
     */
    @Trace
    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<MemberResDto> saveMember(@Valid @RequestBody MemberSaveReqDto request) {
//        Member member = MemberSaveReqDto.from(request);
//        Long savedId = memberService.saveMember(member);

        Long savedId = memberService.saveMember(request);
//        Member savedMember = memberService.findById(savedId);
//        MemberResDto dto = MemberResDto.from(savedMember);
        MemberResDto dto = memberService.toMemberResDto(savedId);

        log.info("회원가입 완료 (memberId={}, username={})", savedId, dto.getUsername());

        return Response.success(dto);
    }

    /**
     * 로그인
     */
    @PostMapping("/members/login")
    @ResponseStatus(HttpStatus.OK)
    public Response<TokenDto> login(final @Valid @RequestBody MemberLoginReqDto request) {

        TokenDto tokenDto = memberService.login(request.getUsername(), request.getPassword());

        return Response.success(tokenDto);
    }


    /**
     * 전체 회원 조회 - ADMIN 가능
     * @param desc id로 내림차순 정렬 여부
     */
    @Secured("ADMIN")
    @GetMapping("/members")
    @ResponseStatus(HttpStatus.OK)
    public Response<Page<MemberResDto>> members(@RequestParam(required = false, defaultValue = "10") int size,  //한 페이지의 데이터 총 개수
                                                @RequestParam(required = false, defaultValue = "0") int page,  //현재 페이지
                                                @RequestParam(required = false, defaultValue = "true") Boolean desc) {  //내림차순 정렬 여부)

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, "id"));
        Page<Member> members = memberRepository.findAll(pageRequest);
        Page<MemberResDto> dtos = members.map(member -> memberService.toMemberResDto(member.getId()));

        return Response.success(dtos);
    }


    /**
     * 개별 회원 조회 by id - 본인, ADMIN 가능
     */
    @PreAuthorize("hasRole('ADMIN') or @authService.hasId(#id)")  //ADMIN or 본인 가능
    @GetMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response<MemberResDto> member(@PathVariable Long id) {
        MemberResDto dto = memberService.toMemberResDto(id);
        return Response.success(dto);
    }


    /**
     * 회원정보 수정 (nickname, age, address, password) - 본인만 가능
     */
    @PreAuthorize("@authService.hasId(#id)")  //본인만 가능
    @PatchMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response<MemberResDto> updateMember(@PathVariable Long id, @Valid @RequestBody MemberUpdateReqDto request) {
        memberService.update(id, request.getCheckPassword(), request.getNewPassword(), request.getNickname(), request.getAge(), request.getAddress());
        MemberResDto dto = memberService.toMemberResDto(id);
        return Response.success(dto);
    }


    /**
     * 회원 탈퇴 - 본인 or ADMIN 가능
     */
    @PreAuthorize("hasRole('ADMIN') or @authService.hasId(#id)")  //ADMIN or 본인 가능
    @DeleteMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response deleteMember(@PathVariable Long id, @Valid @RequestBody MemberDeleteDto request) {
        memberService.deleteMemberWithHistory(id, request.getPassword());
        return Response.success();
    }

}
