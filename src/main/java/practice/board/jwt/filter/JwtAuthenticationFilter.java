package practice.board.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import practice.board.domain.Member;
import practice.board.jwt.service.JwtService;
import practice.board.repository.MemberRepository;

import java.io.IOException;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;
//    private final MemberService memberService;  //TODO 이렇게 의존관계 설정해도 되나 - securityConfig 에도 의존관계 주입... 순환참조 오류?

//    private final String NO_CHECK_URL = "/login";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if (isUrlAndMethodToBypass(requestURI, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtService.extractAccessToken(request).filter(jwtService::isValid).orElse(null);

        //access token 이 유효한 경우
        if (accessToken != null) {

            String username = jwtService.extractUsername(accessToken);
            memberRepository.findByUsername(username).ifPresentOrElse(
                    (member) -> {
//                        jwtService.sendAccessToken(response, accessToken);  //TODO 필요한 이유?? 기존의 accessToken과 동일한데 굳이 다시 보낼 필요 없겠다.
                        log.info("JwtAuthenticationFilter 호출");
                        log.info("accessToken={}", accessToken);
                        saveAuthentication(member, accessToken);  //JWT 의 서명이 유효하기에 Authentication 객체 생성
                    },
                    () -> {throw new NoSuchElementException("해당 username의 회원이 존재하지 않습니다. username= " + username);
                    }
            );
        }

        //access token 이 유효하지 않은 경우
        else {

            String refreshToken = jwtService.extractRefreshToken(request).filter(jwtService::isValid).orElse(null);

            //refresh token 이 유효한 경우
            if (refreshToken != null)  {
                memberRepository.findByRefreshToken(refreshToken).ifPresentOrElse(
                        (member) -> {
                            //access token, refresh token 모두 재발급
                            String newAccessToken = jwtService.createAccessToken(member.getUsername());
                            String newRefreshToken = jwtService.createRefreshToken();
                            jwtService.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);

                            //새로 발급한 refresh token db 에 저장
                            memberRepository.updateRefreshToken(member.getId(), newRefreshToken);

                            saveAuthentication(member, newAccessToken);  //JWT 의 서명이 유효하기에 Authentication 객체 생성
                            log.info("new access token={}", newAccessToken);
                            log.info("refresh token={}", refreshToken);
                        },
                        () -> {throw new NoSuchElementException("invalid refresh token");}
                );
            }

            //refresh token 이 유효하지 않은 경우
            else {
                sendUnauthorizedResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isUrlAndMethodToBypass(String requestURI, String method) {
        if ("/login".equals(requestURI)) {
            return true;
        }

        if ("/api/members".equals(requestURI) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("unauthorized");
    }

    private void saveAuthentication(Member member, String accessToken) {

//        UserDetails user = User.builder()
//                .username(member.getUsername())
//                .password(member.getPassword())
//                .roles(member.getRole().name())
//                .build();

        Authentication authentication = jwtService.getAuthentication(accessToken);
        log.info("authentication={}", authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

}
