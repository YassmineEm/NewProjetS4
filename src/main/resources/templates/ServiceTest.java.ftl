package ${packageName};

import ${packageName?replace(".service", ".model")}.${entityName};
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ${entityName}ServiceTest {

    @Test
    public void testServiceInstance() {
        ${entityName}Service service = mock(${entityName}Service.class);
        assertNotNull(service);
    }

    @Test
    public void testCreateEntity() {
        ${entityName}Service service = mock(${entityName}Service.class);
        ${entityName} entity = new ${entityName}();
        when(service.save(any(${entityName}.class))).thenReturn(entity);

        ${entityName} result = service.save(entity);
        assertNotNull(result);
    }
}

