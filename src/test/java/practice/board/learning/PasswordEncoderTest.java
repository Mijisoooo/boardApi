package practice.board.learning;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

public class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    void password_encode_test() {
        //given
        String password = "가나다라마";

        //when
        String encoded = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);

        //then
        assertThat(encoded).isNotEqualTo(password);
        assertThat(encoded).contains("{bcrypt}");
        assertThat(encoded).startsWith("{bcrypt}");
        assertThat(encoded).isNotEqualTo(encoded2);
    }

    @Test
    void password_match_test() {
        //given
        String password = "abcde112";

        //when
        String encoded = passwordEncoder.encode(password);

        //then
        assertThat(passwordEncoder.matches(password, encoded)).isTrue();
    }

}
