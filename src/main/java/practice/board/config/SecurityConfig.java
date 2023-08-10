package practice.board.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import practice.board.repository.MemberRepository;
import practice.board.service.LoginService;
import practice.board.jwt.filter.JwtAuthenticationFilter;
import practice.board.jwt.service.JwtService;
import practice.board.jwt.filter.JsonUsernamePasswordAuthenticationFilter;
import practice.board.jwt.handler.LoginFailureHandler;
import practice.board.jwt.handler.LoginSuccessJwtProvideHandler;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final LoginService loginService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    private final String[] whiteList = {"/", "/login"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .addFilterAfter(jsonUsernamePasswordLoginFilter(), LogoutFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, memberRepository), JsonUsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                        .anyRequest().authenticated())  //그 외 모든 요청은 인증 필요
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  //세션 사용하지 않음
                .build();
    }

    //암호를 암호화하거나, 사용자가 입력한 암호가 기존 암호와 일치하는지 검사할 때 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

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
        daoAuthProvider.setUserDetailsService(loginService);
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


}
