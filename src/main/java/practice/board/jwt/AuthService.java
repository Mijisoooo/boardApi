package practice.board.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import practice.board.domain.Member;
import practice.board.repository.MemberRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;

    public static String getLoginUsername() {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUsername();
    }

    public boolean hasId(Long id) {
        Optional<Member> optionalMember = memberRepository.findByUsername(getLoginUsername());
        return optionalMember.map(member -> member.getId().equals(id)).orElse(false);
    }


}
