package practice.board.web.dto.member;

import lombok.*;
import practice.board.domain.Address;
import practice.board.domain.Member;

@Data
public class MemberResDto {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private Integer age;
    private Address address;


    @Builder
    public MemberResDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.age = member.getAge();
        this.address = member.getAddress();
    }
}
