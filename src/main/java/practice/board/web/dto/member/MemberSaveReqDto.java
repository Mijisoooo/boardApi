package practice.board.web.dto.member;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Range;
import practice.board.domain.Address;
import practice.board.domain.Member;


@Data
public class MemberSaveReqDto {

    @NotEmpty
    private String username;  //아이디

    @NotEmpty
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[._?!*])[a-zA-Z\\d._?!*]{8,20}$",
            message = "비밀번호는 적어도 한개의 대문자, 소문자, 숫자, 특수기호(._?!*)를 포함하며, 8자리 이상 20자리 이하여야 합니다.")
    private String password;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "이메일 형식이 아닙니다.")
    private String email;

    @NotEmpty
    private String nickname;  //별명

    @NotNull
    @Range(min = 1, max = 150)
    private Integer age;  //나이

    private Address address;  //주소

    public static Member from(MemberSaveReqDto dto) {
        return Member.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .age(dto.getAge())
                .address(dto.getAddress())
                .build();
    }

}