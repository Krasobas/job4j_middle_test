package ru.job4j.s.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.job4j.s.service.PhotoService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalPhotoControllerTest {

    @Mock
    private PhotoService photoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            new InternalPhotoController(photoService)
        ).build();
    }

    @Test
    void getPhotoReturns404WhenNotFound() throws Exception {
        when(photoService.exists("missing.jpg")).thenReturn(false);

        mockMvc.perform(get("/internal/photos/missing.jpg"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getPhotoReturns200WhenFound() throws Exception {
        byte[] photo = new byte[]{1, 2, 3};
        when(photoService.exists("photo.jpg")).thenReturn(true);
        when(photoService.getPhoto("photo.jpg")).thenReturn(photo);

        mockMvc.perform(get("/internal/photos/photo.jpg"))
            .andExpect(status().isOk());
    }
}