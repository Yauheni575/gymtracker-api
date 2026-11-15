package com.gym.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    private MockMvc mvc;

    @Test
    void loginWithRightAndWrongPassword() throws Exception {
        String login = register();

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, "wrong-password")))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, "secret123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, "secret123")))
                .andExpect(status().isConflict());
    }

    @Test
    void userCannotTouchSomeoneElsesData() throws Exception {
        String a = token(register());
        String b = token(register());
        long exercise = createExercise(a, "Bench");
        long entry = addEntry(a, exercise, "2026-11-15", 80, 5);

        mvc.perform(get("/api/exercises").header("Authorization", b))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/exercises/" + exercise + "/entries").header("Authorization", b))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/entries/" + entry).header("Authorization", b))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/exercises/" + exercise).header("Authorization", b))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/exercises/" + exercise + "/entries").header("Authorization", a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void volumeCountsOnlyOwnSets() throws Exception {
        String a = token(register());
        String b = token(register());
        long bench = createExercise(a, "Bench");
        addEntry(a, bench, "2026-11-10", 80, 5);
        addEntry(a, bench, "2026-11-12", 85, 3);
        long foreign = createExercise(b, "Bench");
        addEntry(b, foreign, "2026-11-12", 200, 10);

        mvc.perform(get("/api/stats/volume?from=2026-11-10&to=2026-11-14").header("Authorization", a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(655.0))
                .andExpect(jsonPath("$.sets").value(2))
                .andExpect(jsonPath("$.days.length()").value(2));

        mvc.perform(get("/api/stats/records").header("Authorization", a))
                .andExpect(jsonPath("$[0].maxWeight").value(85.0))
                .andExpect(jsonPath("$[0].sets").value(2));
    }

    private String register() throws Exception {
        String login = "u" + UUID.randomUUID().toString().substring(0, 8);
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, "secret123")))
                .andExpect(status().isCreated());
        return login;
    }

    private String token(String login) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(login, "secret123")))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }

    private long createExercise(String token, String name) throws Exception {
        String body = mvc.perform(post("/api/exercises").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"favorite\":0}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long addEntry(String token, long exercise, String date, double weight, int reps) throws Exception {
        String body = mvc.perform(post("/api/exercises/" + exercise + "/entries").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + date + "\",\"weight\":" + weight + ",\"reps\":" + reps + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static String credentials(String login, String password) {
        return "{\"login\":\"" + login + "\",\"password\":\"" + password + "\"}";
    }
}
