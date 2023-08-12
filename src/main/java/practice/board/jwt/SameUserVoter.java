package practice.board.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;

@RequiredArgsConstructor
public class SameUserVoter implements AccessDecisionVoter {

    private final AuthService authService;  //TODO authService 사용 가능?


    @Override
    public int vote(Authentication authentication, Object object, Collection collection) {
        if (authentication == null || object == null) {
            return ACCESS_DENIED;
        }

        String requestURI = (String) object;  // requestURI : "/api/members/{id}"

        //memberId 추출
        String[] parts = requestURI.split("/");
        if (parts.length != 4 || !"api".equals(parts[1]) || !"members".equals(parts[2])) {
            return ACCESS_DENIED;
        }

        long memberId;
        try {
            memberId = Long.parseLong(parts[3]);
        } catch (NumberFormatException ex) {
            return ACCESS_DENIED;
        }

        //URI에서 추출한 memberId 와 authentication의 username 의 memberId 비교
        if (authService.hasId(memberId)) {
            return ACCESS_GRANTED;
        }


        return ACCESS_DENIED;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class clazz) {
        return true;
    }
}
