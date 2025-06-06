package ${packageName};


import ${packageName?replace(".controller", ".service")}.${entityName}Service;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

@WebMvcTest(${entityName}Controller.class)
public class ${entityName}ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ${entityName}Service service;

    @Test
    public void testGetAllEntities() throws Exception {
        when(service.findAll()).thenReturn(new java.util.ArrayList<>());
        mockMvc.perform(get("/${entityName?lower_case}s"))
               .andExpect(status().isOk());
    }
}
