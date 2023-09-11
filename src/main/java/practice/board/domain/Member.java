package practice.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.*;

@Table(uniqueConstraints = {
        @UniqueConstraint(name = "member_username_unique", columnNames = {"username"}),
        @UniqueConstraint(name = "member_email_unique", columnNames = {"email"}),
        @UniqueConstraint(name = "member_nickname_unique", columnNames = {"nickname"})
})
@Entity
@Getter
@Setter(value = PRIVATE)
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(access = PRIVATE)  //TODO builder 패턴과 factory 패턴을 섞어서 쓰는데,,, 이러면 산발적으로 여기저기서 객체가 생성되니까 나중에 유지보수 어렵겠다. (the single responsibility principle and avoid code duplication)
                            //TODO -> access = PRIVATE 로 설정 or 클래스에 @Builder를 붙이지 말고 생성자에 Builder를 붙여서 사용
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id")
    private Long id;

    @NotNull
    @Size(min = 1, max = 30)
    @Column(updatable = false)
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

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<LikeArticle> likeArticleList = new ArrayList<>();  //TODO 만들 필요가 있을까?



    //탈퇴 사용자 생성을 위한 필드 (NotNull 컬럼) TODO 상수들을 하나의 클래스에 모아놔야겠다!!!
    private static final String DELETED_MEMBER_USERNAME = "탈퇴한 사용자";
    private static final String DELETED_MEMBER_NICKNAME = "탈퇴한 사용자";
    private static final String DELETED_MEMBER_PASSWORD = "password1234!";
    private static final String DELETED_MEMBER_EMAIL = "deletedMember@email.com";




    //== 정보 수정 ==//
    public void updateRole(Role role) {
        this.role = role;
    }

    /**
     * USER 권한 부여
     */
    public void addUserRole() {
        this.role = Role.USER;
    }


    /**
     * ADMIN 권한 부여
     */
    public void addAdminRole() {
        this.setRole(Role.ADMIN);
    }

    public void updateNickname(String nickname) {
        this.setNickname(nickname);
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

//    public void addLikeList(Article article) {  //TODO article.increaseLikes() 랑 묶어줄 수 있으면 좋겠다.
//        this.getLikeList().add(article);
//    }

//    public void removeLikeList(Article article) {  //TODO article.decreaseLikes() 랑 묶어줄 수 있으면 좋겠다.
//        this.getLikeList().remove(article);
//    }




    /**
     * 비밀번호 일치 여부 판단
     * @param checkPassword 입력한 password
     */
    public boolean validatePassword(PasswordEncoder passwordEncoder, String checkPassword) {
        return passwordEncoder.matches(checkPassword, getPassword());
    }


    /**
     * 패스워드 암호화
     */
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }


    //== 연관관계 메서드 ==//


    //== 생성 메서드 ==//
    //생성메서드를 만들어서 관리를 한다면 코드가 변경되더라도 변경 지점이 한 곳이 됨
    public static Member createMember(String username, String password, String email, String nickname, Integer age, Address address) {

        Member member = Member.builder()
                .username(username)
                .password(password)  //TODO 여기서 암호화해야하나 service단 말고?
                .email(email)
                .nickname(nickname)
                .age(age)
                .address(address)
                .build();

        return member;
    }

    //탈퇴한 사용자 생성
    public static Member createDeletedMember() {
        return Member.builder()
                .username(DELETED_MEMBER_USERNAME)
                .password(DELETED_MEMBER_PASSWORD)
                .nickname(DELETED_MEMBER_NICKNAME)
                .email(DELETED_MEMBER_EMAIL)
                .build();
    }


    //== 비즈니스 로직 ==//



    //== 조회 로직 ==//



}
