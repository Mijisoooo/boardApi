package practice.board.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper;

    private static final String DEFAULT_LOGIN_REQUEST_URL = "/login";  // /login/oauth2/ + ????? 로 오는 요청을 처리할 것이다
    private static final String HTTP_METHOD = "POST";    //HTTP 메서드의 방식은 POST 이다.
    private static final String CONTENT_TYPE = "application/json";//json 타입의 데이터로만 로그인을 진행한다.

    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";


    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER
            = new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD);


    public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);  //위에서 설정한 POST "/login/**" 요청을 처리하기 위해 설정
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        if (request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported. content-type:" + request.getContentType());
        }

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        Map<String, String> usernamePasswordMap = objectMapper.readValue(messageBody, Map.class);  //TODO 왜 Map으로 받아왔을까?
        String username = usernamePasswordMap.get(USERNAME_KEY);
        String password = usernamePasswordMap.get(PASSWORD_KEY);

        //JSON으로 로그인하는 방식만 달라졌을 뿐, username, password를 사용하여 로그인하는 전략은 동일하기 때문에 굳이 따로 구현하지 않고 기존 것 사용
        UsernamePasswordAuthenticationToken authRequest
                = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
//        // Allow subclasses to set the "details" property
//        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);  //사용되는 AuthenticationManager는 ProviderManager
    }

}
