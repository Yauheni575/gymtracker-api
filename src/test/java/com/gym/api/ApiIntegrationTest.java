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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void syncMovesDataBetweenDevicesAndPropagatesDeletes() throws Exception {
        String a = token(register());
        String id = UUID.randomUUID().toString().substring(0, 8);
        String push = """
                {"cursor":0,
                 "exercises":[{"uuid":"ex-ID","name":"Bench","favorite":127,"deleted":false}],
                 "entries":[{"uuid":"en1-ID","exerciseUuid":"ex-ID","date":"2026-11-14","weight":80,"reps":5,"deleted":false},
                            {"uuid":"en2-ID","exerciseUuid":"ex-ID","date":"2026-11-15","weight":82.5,"reps":3,"deleted":false}],
                 "bodyWeights":[{"date":"2026-11-15","weight":80.5,"deleted":false}],
                 "foods":[{"uuid":"f-ID","date":"2026-11-15","name":"Rice","grams":200,"kcal":130,"protein":3,"fat":1,"carbs":28,"deleted":false}],
                 "profile":{"name":"Zhenya","sex":"M","age":20,"height":180,"target":85,"goal":"Mass","experience":"Novice","perWeek":3,"priorities":""}}
                """.replace("ID", id);
        String first = sync(a, push);
        long cursor = ((Number) JsonPath.read(first, "$.cursor")).longValue();

        String secondDevice = sync(a, "{\"cursor\":0}");
        assertThat((Integer) JsonPath.read(secondDevice, "$.exercises.length()")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(secondDevice, "$.entries.length()")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(secondDevice, "$.foods.length()")).isEqualTo(1);
        assertThat((String) JsonPath.read(secondDevice, "$.profile.name")).isEqualTo("Zhenya");
        assertThat((Double) JsonPath.read(secondDevice, "$.bodyWeights[0].weight")).isEqualTo(80.5);

        String afterDelete = sync(a, "{\"cursor\":" + cursor + ",\"entries\":[{\"uuid\":\"en2-" + id + "\",\"deleted\":true}]}");
        assertThat((List<Boolean>) JsonPath.read(afterDelete, "$.entries[*].deleted")).containsExactly(true);
        cursor = ((Number) JsonPath.read(afterDelete, "$.cursor")).longValue();

        long exerciseId = ((Number) JsonPath.read(mvc.perform(get("/api/exercises").header("Authorization", a))
                .andReturn().getResponse().getContentAsString(), "$[0].id")).longValue();
        mvc.perform(get("/api/exercises/" + exerciseId + "/entries").header("Authorization", a))
                .andExpect(jsonPath("$.page.totalElements").value(1));
        mvc.perform(delete("/api/exercises/" + exerciseId).header("Authorization", a))
                .andExpect(status().isNoContent());

        String afterRestDelete = sync(a, "{\"cursor\":" + cursor + "}");
        assertThat((List<Boolean>) JsonPath.read(afterRestDelete, "$.exercises[*].deleted")).containsExactly(true);
        assertThat((List<Boolean>) JsonPath.read(afterRestDelete, "$.entries[*].deleted")).containsExactly(true);
        mvc.perform(get("/api/exercises").header("Authorization", a))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void syncCannotOverwriteSomeoneElsesUuidAndRenamesDuplicates() throws Exception {
        String a = token(register());
        String b = token(register());
        String id = UUID.randomUUID().toString().substring(0, 8);
        sync(a, "{\"cursor\":0,\"exercises\":[{\"uuid\":\"x-" + id + "\",\"name\":\"Bench\",\"favorite\":0,\"deleted\":false}]}");

        sync(b, "{\"cursor\":0,\"exercises\":[{\"uuid\":\"x-" + id + "\",\"name\":\"Hacked\",\"favorite\":0,\"deleted\":false}]}");
        sync(b, "{\"cursor\":0,\"exercises\":[{\"uuid\":\"x-" + id + "\",\"deleted\":true}]}");
        mvc.perform(get("/api/exercises").header("Authorization", a))
                .andExpect(jsonPath("$[0].name").value("Bench"));

        String dup = sync(a, "{\"cursor\":0,\"exercises\":[{\"uuid\":\"y-" + id + "\",\"name\":\"Bench\",\"favorite\":0,\"deleted\":false}]}");
        assertThat((List<String>) JsonPath.read(dup, "$.exercises[*].name"))
                .containsExactlyInAnyOrder("Bench", "Bench (2)");
    }

    private String sync(String token, String body) throws Exception {
        return mvc.perform(post("/api/sync").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
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
