package practice.board.web.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import practice.board.domain.Address;

@Data
public class MemberUpdateReqDto {

    @NotBlank
    private String checkPassword;  //회원정보 수정 전, 비밀번호 확인을 위해

    @Pattern(regexp = "^(?:(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[._?!*])[a-zA-Z\\d._?!*]{8,20})?$",  //null 허용
            message = "비밀번호는 적어도 한개의 대문자, 소문자, 숫자, 특수기호(._?!*)를 포함하며, 8자리 이상 20자리 이하여야 합니다.")
    private String newPassword;

    private String nickname;
    private Integer age;
    private Address address;

}
