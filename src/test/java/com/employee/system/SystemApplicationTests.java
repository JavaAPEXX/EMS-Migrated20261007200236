import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SystemApplicationTests {

    @Test
    @DisplayName("applicationContextLoads")
    void applicationContextLoads() {
        assertTrue(true);
    }
}
