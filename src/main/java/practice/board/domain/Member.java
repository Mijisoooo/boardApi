package practice.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Table(uniqueConstraints = {
        @UniqueConstraint(name = "member_username_unique", columnNames = {"username"}),
        @UniqueConstraint(name = "member_email_unique", columnNames = {"email"}),
        @UniqueConstraint(name = "member_username_unique", columnNames = {"nickname"})
})
@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotNull
    @Size(min = 1, max = 30)
    private String username;  //아이디

    @NotNull
    private String password;  //비밀번호

    @NotNull
    @Column(updatable = false)
    private String email;  //이메일

    @NotNull
    @Size(min = 1, max = 30)
    private String nickname; //별명

    private Integer age;  //나이

    @Embedded
    private Address address;  //주소

    @Enumerated(EnumType.STRING)
    private Role role;  //권한 [ADMIN, USER]

    @Column(length = 1000)
    private String refreshToken;

    @Builder.Default
    @OneToMany(mappedBy = "writer")
    private List<Article> articleList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "writer")
    private List<Comment> commentList = new ArrayList<>();


    //== 정보 수정 ==//
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * USER 권한 부여
     */
    public void addUserRole() {
        this.role = Role.USER;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(PasswordEncoder passwordEncoder, String password) {
        this.password = passwordEncoder.encode(password);
    }

    public void updateAge(int age) {
        this.age = age;
    }

    public void updateAddress(Address address) {
        this.address = address;
    }

    public void update(String password, Address address) {
        this.setPassword(password);
        this.setAddress(address);
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }


    /**
     * 회원정보 수정, 회원 탈퇴 전에 비밀번호 확인 진행
     * 이때 비밀번호 일치여부 판단하는 메서드
     * @param checkPassword 입력한 password
     */
    public boolean matchPassword(PasswordEncoder passwordEncoder, String checkPassword) {
        return passwordEncoder.matches(checkPassword, getPassword());
    }


    /**
     * 패스워드 암호화
     */
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }


    //== 연관관계 메서드 ==//


    //== 생성 메서드 ==// TODO builder 사용??
    public static Member createMember(String username, String email, String nickname, Integer age, Address address) {
        Member member = new Member();
        member.username = username;
        member.email = email;
        member.nickname = nickname;
        member.age = age;
        member.address = address;
        return member;

    }


    //== 비즈니스 로직 ==//



    //== 조회 로직 ==//



}
