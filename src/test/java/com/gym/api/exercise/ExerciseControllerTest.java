package com.gym.api.exercise;

import com.gym.api.auth.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseController.class)
@Import(SecurityConfig.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ExerciseService service;

    @Test
    void requiresToken() throws Exception {
        mvc.perform(get("/api/exercises"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passesUserIdFromToken() throws Exception {
        when(service.findAll(7L)).thenReturn(List.of(new ExerciseResponse(1L, "Bench", 127)));

        mvc.perform(get("/api/exercises").with(jwt().jwt(j -> j.subject("7"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bench"))
                .andExpect(jsonPath("$[0].favorite").value(127));

        verify(service).findAll(7L);
    }

    @Test
    void returnsFieldErrorsOnInvalidBody() throws Exception {
        mvc.perform(post("/api/exercises")
                        .with(jwt().jwt(j -> j.subject("7")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"favorite\":200}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.favorite").exists());

        verify(service, never()).create(anyLong(), any());
    }

    @Test
    void returnsBadRequestOnBrokenJson() throws Exception {
        mvc.perform(post("/api/exercises")
                        .with(jwt().jwt(j -> j.subject("7")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Некорректный JSON или формат поля"));
    }
}
