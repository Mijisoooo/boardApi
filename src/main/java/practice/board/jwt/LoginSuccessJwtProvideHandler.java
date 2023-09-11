package practice.board.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import practice.board.repository.MemberRepository;


@RequiredArgsConstructor
@Slf4j
public class LoginSuccessJwtProvideHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String username = getUsername(authentication);

        //JWT 발급
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();

        //응답 헤더로 access token, refresh token 보내기
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        //refresh token을 db에 저장
        memberRepository.findByUsername(username).ifPresent(member -> member.updateRefreshToken(refreshToken));


        log.info("로그인 성공. username: {}, accessToken, refreshToken 발급. accessToken: {}, refreshToken: {}", username, accessToken, refreshToken);

    }

    private static String getUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
