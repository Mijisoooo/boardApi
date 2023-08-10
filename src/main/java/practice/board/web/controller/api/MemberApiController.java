package practice.board.web.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import practice.board.domain.Member;
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

    /**
     * 전체 회원 조회
     */
    @GetMapping("/members")
    @ResponseStatus(HttpStatus.OK)
    public Response members() {
        List<Member> members = memberService.findAll();
        List<MemberResDto> dtos = members.stream()
                .map(m -> new MemberResDto(m))
                .collect(Collectors.toList());

        return Response.success(dtos);
    }

    /**
     * 회원 조회 by id
     */
    @GetMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response member(@PathVariable Long id) {
        Member member = memberService.findById(id);
        MemberResDto dto = new MemberResDto(member);
        return Response.success(dto);
    }

    /**
     * 회원 저장
     */
    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Response saveMember(@Valid @RequestBody MemberSaveReqDto request) {
        Member member = MemberSaveReqDto.from(request);
        Long savedId = memberService.join(member);
        Member savedMember = memberService.findById(savedId);
        MemberResDto dto = new MemberResDto(savedMember);
        return Response.success(dto);
    }

    /**
     * 회원정보 수정 (nickname, age, address, password)
     */
    @PatchMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response updateMember(@PathVariable Long id, @Valid @RequestBody MemberUpdateReqDto request) {
        memberService.update(id, request.getCheckPassword(), request.getNewPassword(), request.getNickname(), request.getAge(), request.getAddress());
        Member member = memberService.findById(id);
        MemberResDto dto = new MemberResDto(member);
        return Response.success(dto);
    }


    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/members/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response deleteMember(@PathVariable Long id, @Valid @RequestBody MemberDeleteDto request) {
        memberService.delete(id, request.getPassword());
        return Response.success();
    }

}
