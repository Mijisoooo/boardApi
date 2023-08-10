package practice.board.jwt.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.Optional;

public interface JwtService {

    String createAccessToken(String username);
    String createRefreshToken();

    void updateRefreshToken(String username, String refreshToken);
    void destroyRefreshToken(String username);

    void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken);
    void sendAccessToken(HttpServletResponse response, String accessToken);

    Optional<String> extractAccessToken(HttpServletRequest request);
    Optional<String> extractRefreshToken(HttpServletRequest request);
    String extractUsername(String accessToken);

    Authentication getAuthentication(String accessToken);

    void setAccessTokenHeader(HttpServletResponse response, String accessToken);

    void setRefreshTokenHeader(HttpServletResponse response, String refreshToken);

    boolean isValid(String token);

}
