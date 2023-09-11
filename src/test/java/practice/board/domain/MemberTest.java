package practice.board.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void create() {
        Address address = new Address("city", "street", "zipcode");
        Member member = Member.createMember("username", "password123!", "test@email.com", "nick", 20, address);

        Assertions.assertThat(member.getAddress()).isEqualTo(address);
    }

    @Test
    void update() {
        Address address = new Address("city", "street", "zipcode");
        Member member = Member.createMember("username", "password123!", "test@email.com", "nick", 20, address);

        Address newAddress = new Address("testCity", "testStreet", "12345");
        member.updateAddress(newAddress);

        Assertions.assertThat(member.getPassword()).isEqualTo("1234");
        Assertions.assertThat(member.getAddress()).isEqualTo(newAddress);
    }


}