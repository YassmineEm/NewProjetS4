package ${packageName};

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ${entityName}ServiceTest {

    @Autowired
    private ${entityName}Service service;

    @Test
    public void testServiceNotNull() {
        assertNotNull(service);
    }

    @Test
    public void testCreateEntity() {
        // TODO : compléter le test
    }
}