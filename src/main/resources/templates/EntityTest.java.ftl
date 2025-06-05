package ${packageName}.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ${entityName}Test {

    @Test
    public void testEntityNotNull() {
        ${entityName} entity = new ${entityName}();
        assertNotNull(entity);
    }

    @Test
    public void testEntityProperties() {
        // TODO: compléter les tests des propriétés de ${entityName}
    }
}

