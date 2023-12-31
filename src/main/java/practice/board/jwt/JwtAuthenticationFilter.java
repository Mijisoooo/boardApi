package practice.board.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import practice.board.exception.ApiException;
import practice.board.repository.MemberRepository;
import practice.board.service.MemberService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static practice.board.exception.ErrorCode.*;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final MemberService memberService;  //TODO 이렇게 의존관계 설정해도 되나 - securityConfig 에도 의존관계 주입... 순환참조 오류?

    private final List<String> NO_CHECK_URL = Arrays.asList("/api/members/login", "/api/members");


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        //필터 적용 여부 체크
        if (!isFilterApplicable(requestURI, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        //access token 꺼내기
        String accessToken = jwtService.extractAccessToken(request).filter(jwtService::isValid).orElse(null);

        //access token 이 유효한 경우 : jwt 토큰의 인증정보를 현재 쓰레드의 SecurityContext 에 저장
        if (StringUtils.hasText(accessToken)) {

            String username = jwtService.extractUsername(accessToken);
            memberRepository.findByUsername(username).ifPresentOrElse(
                    (member) -> {
//                        jwtService.sendAccessToken(response, accessToken);  //TODO 필요한 이유?? 기존의 accessToken과 동일한데 굳이 다시 보낼 필요 없겠다.
                        log.info("JwtAuthenticationFilter 호출. accessToken={}", accessToken);
                        jwtService.saveAuthentication(accessToken);  //JWT 의 서명이 유효하기에 Authentication 객체 생성하여 SecurityContext 에 저장
                    },
                    () -> {throw new ApiException(MEMBER_NOT_FOUND, "해당 username의 회원이 존재하지 않습니다. username= " + username);
                        //TODO 어떤 예외를 터뜨려야 하나??
                    }
            );
        }

        //access token 이 만료된 경우 : refresh token 유효성 검증
        //-> TODO access token 이 없거나 유효하지 않은 경우가 아니라, 그중에서도 만료된 경우에만 refresh token 체크하려고 함!!
        else {

            Optional<String> refreshTokenOptional = jwtService.extractRefreshToken(request);

            //refresh token 이 없는 경우 : refresh token 요청하는 예외 발생
            if (refreshTokenOptional.isEmpty()) {
                throw new ApiException(EXPIRED_ACCESS_TOKEN);  //TODO 메세지 수정 필요 or 에러 자체 수정 필요 - 여기서는 accessToken 자체가 없는 상태
            }

            String refreshToken = refreshTokenOptional.filter(jwtService::isValid).orElse(null);

            //refresh token 이 유효한 경우
            if (StringUtils.hasText(refreshToken)) {
                memberRepository.findByRefreshToken(refreshToken).ifPresentOrElse(
                        (member) -> {
                            //1) access token, refresh token 모두 재발급
                            String newAccessToken = jwtService.createAccessToken(member.getUsername());
                            String newRefreshToken = jwtService.createRefreshToken();
                            jwtService.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);

                            //2) 새로 발급한 refresh token 을 db 에 저장
                            memberService.updateRefreshToken(member.getId(), newRefreshToken);

                            //3) JWT 의 서명이 유효하기에 Authentication 객체 생성하여 SecurityContext 에 저장
                            jwtService.saveAuthentication(newAccessToken);
                            log.info("new access token={}, refresh token={}", newAccessToken, refreshToken);
                        },
                        () -> {throw new ApiException(MEMBER_NOT_FOUND);}
                );
            }

            //refresh token 이 유효하지 않은 경우
            else {
                throw new ApiException(INVALID_TOKEN, "refresh token 이 유효하지 않습니다.");
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 필터 적용 여부 체크 (URI + httpMethod)
     * 인증 필요 없는 요청인 경우, false 리턴
     */
    private boolean isFilterApplicable(String requestURI, String method) {

        if (requestURI == null || method == null) {
            return false;
        }

        //POST - "/api/members/login", POST - "/api/members"
        if (NO_CHECK_URL.contains(requestURI) && "POST".equalsIgnoreCase(method)) {
            return false;
        }

        //GET - "/api/articles", "/api/articles/{id}"
        if (requestURI.startsWith("/api/articles")  && "GET".equalsIgnoreCase(method)) {
            return false;
        }

        //GET - "/api/comments", "/api/comments/{id}"
        if (requestURI.startsWith("/api/comments") && "GET".equalsIgnoreCase(method)) {
                return false;
        }

        return true;
    }

}
