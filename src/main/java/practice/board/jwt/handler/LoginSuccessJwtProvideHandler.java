package practice.board.jwt.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import practice.board.jwt.service.JwtService;
import practice.board.repository.MemberRepository;

import java.io.IOException;


@RequiredArgsConstructor
@Slf4j
public class LoginSuccessJwtProvideHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = getUsername(authentication);

        //JWT 발급
        String accessToken = jwtService.createAccessToken(username);
        String refreshToken = jwtService.createRefreshToken();
        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);  //TODO refreshToken도 헤더에 담아서 보냄??

        memberRepository.findByUsername(username).ifPresent(member -> member.updateRefreshToken(refreshToken));

        log.info("로그인 성공. username: {}", username);
        log.info("accessToken 발급. accessToken: {}", accessToken);
        log.info("refreshToken 발급. refreshToken: {}", refreshToken);

    }

    private static String getUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
