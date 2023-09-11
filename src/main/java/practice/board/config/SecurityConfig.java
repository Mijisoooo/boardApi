package practice.board.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import practice.board.jwt.*;
import practice.board.repository.MemberRepository;
import practice.board.service.CustomUserDetailsService;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
@EnableMethodSecurity  //@PreAuthorize, @PostAuthorize, @Secured, @RolesAllowed 어노테이션 사용을 위해 추가
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;


    private final String[] whiteList = {"/", "/api/members/login"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                //csrf 보안 해제 (실무에서는 보안 적용)
                .csrf(AbstractHttpConfigurer::disable)

                //cors 설정
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))

                //h2 db 사용
                .headers(config -> config.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                //filter 추가
//                .addFilterAfter(jsonUsernamePasswordLoginFilter(), LogoutFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, memberRepository), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()  //Preflight 요청 허용
                        .requestMatchers(whiteList).permitAll()  //whiteList - 인증 필요 없음
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(POST, "/api/members").permitAll()  //회원 가입 - 인증 필요 없음
//                        .requestMatchers(GET, "/api/members/**").hasRole("ADMIN")  //회원 조회 - ADMIN
                        .requestMatchers(GET, "/api/articles/**").permitAll()  //글 조회 - 인증 필요 없음
                        .requestMatchers(GET, "/api/comments/**").permitAll()  //댓글 조회 - 인증 필요 없음
                        .requestMatchers(POST, "/api/articles").authenticated()  //글 작성 - 인증 필요 (권한 상관없음)
                        .anyRequest().authenticated())  //그 외 모든 요청은 인증 필요

                .exceptionHandling(config -> config
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler))

                .sessionManagement(config -> config
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .build();
    }



    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("https://localhost:3000"));
//        config.setAllowedOrigins("^https?:\\/\\/https://main--chimerical-malabi-ffde60.netlify.app/$");  //TODO regex 사용하는 법 찾아보기
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    //암호를 암호화하거나, 사용자가 입력한 암호가 기존 암호와 일치하는지 검사할 때 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /*
    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter() {
        JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter
                = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        jsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        jsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessJwtProvideHandler());
        jsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return jsonUsernamePasswordLoginFilter;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthProvider = new DaoAuthenticationProvider();
        daoAuthProvider.setUserDetailsService(customUserDetailsService);
        daoAuthProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(daoAuthenticationProvider());
    }

    @Bean
    public LoginSuccessJwtProvideHandler loginSuccessJwtProvideHandler() {
        return new LoginSuccessJwtProvideHandler(jwtService, memberRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }
*/

}
