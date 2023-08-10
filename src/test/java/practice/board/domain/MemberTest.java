package practice.board.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void create() {
        Address address = new Address("city", "street", "zipcode");
        Member member = Member.builder()
                .username("username")
                .password("1234")
                .email("email")
                .nickname("nickname")
                .address(address)
                .build();
        Assertions.assertThat(member.getAddress()).isEqualTo(address);
    }

    @Test
    void update() {
        Address address = new Address("city", "street", "zipcode");
        Member member = Member.builder()
                .username("username")
                .password("1234")
                .email("email")
                .nickname("nickname")
                .address(address)
                .build();

        Address newAddress = new Address("testCity", "testStreet", "12345");
        member.updateAddress(newAddress);

        Assertions.assertThat(member.getPassword()).isEqualTo("1234");
        Assertions.assertThat(member.getAddress()).isEqualTo(newAddress);
    }


}