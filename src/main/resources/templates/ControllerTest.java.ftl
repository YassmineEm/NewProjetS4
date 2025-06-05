package ${packageName};

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(${entityName}Controller.class)
public class ${entityName}ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllEntities() throws Exception {
        mockMvc.perform(get("/${entityName?lower_case}s"))
               .andExpect(status().isOk());
    }
}
