package ru.job4j.r.controller;

import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentRestControllerTest {

    @Mock
    private ProducerTemplate producerTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            new StudentRestController(producerTemplate)
        ).build();
    }

    @Test
    void getAllStudentsReturns200() throws Exception {
        when(producerTemplate.requestBody("direct:getAllStudents", "ALL", String.class))
            .thenReturn("[{\"lastName\":\"Doe\"}]");

        mockMvc.perform(get("/api/students"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getStudentReturns200() throws Exception {
        when(producerTemplate.requestBody("direct:getStudent", "RB-001", String.class))
            .thenReturn("{\"lastName\":\"Doe\"}");

        mockMvc.perform(get("/api/students/RB-001"))
            .andExpect(status().isOk());
    }

    @Test
    void getPhotoReturns404WhenPhotoIsEmpty() throws Exception {
        when(producerTemplate.requestBody("direct:getPhoto", "RB-001"))
            .thenReturn(new byte[0]);

        mockMvc.perform(get("/api/students/RB-001/photo"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getPhotoReturns200WhenPhotoExists() throws Exception {
        byte[] photo = new byte[]{1, 2, 3};
        when(producerTemplate.requestBody("direct:getPhoto", "RB-002"))
            .thenReturn(photo);

        mockMvc.perform(get("/api/students/RB-002/photo"))
            .andExpect(status().isOk());
    }
}