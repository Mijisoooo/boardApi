package practice.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Board230719Application {

    public static void main(String[] args) {
        SpringApplication.run(Board230719Application.class, args);
    }

}
