package ifsc.edu.lll;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "jwt.secret=segredo-muito-seguro-com-mais-de-32-bytes-para-os-testes")
class LllApplicationTests {

    @Test
    void contextLoads() {
    }

}
