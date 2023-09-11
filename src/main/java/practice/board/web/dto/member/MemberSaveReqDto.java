package practice.board.web.dto.member;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Range;
import practice.board.domain.Address;
import practice.board.domain.Member;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSaveReqDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String username;  //아이디

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[._?!*])[a-zA-Z\\d._?!*]{8,20}$",
            message = "비밀번호는 적어도 한개의 대문자, 소문자, 숫자, 특수기호(._?!*)를 포함하며, 8자 이상 20자 이하여야 합니다.")
    private String password;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "이메일 형식으로 입력해주세요.")
    private String email;

    @NotBlank(message = "별명을 입력해주세요.")
    private String nickname;  //별명

    @Range(min = 1, max = 150)
    private Integer age;  //나이

    private Address address;  //주소


    //== 변환 메서드 ==//
    //MemberSaveReqDto -> Member
    public static Member from(MemberSaveReqDto dto) {
        return Member.createMember(dto.getUsername(), dto.getPassword(), dto.getEmail(), dto.getNickname(), dto.getAge(), dto.getAddress());
    }

    //Member -> MemberSaveReqDto
    public static MemberSaveReqDto toDto(Member member) {
        return MemberSaveReqDto.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .age(member.getAge())
                .address(member.getAddress())
                .build();
    }

}
