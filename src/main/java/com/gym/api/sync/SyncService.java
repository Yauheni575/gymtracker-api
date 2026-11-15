package com.gym.api.sync;

import com.gym.api.profile.ProfileDto;
import com.gym.api.sync.SyncData.BodyWeightChange;
import com.gym.api.sync.SyncData.EntryChange;
import com.gym.api.sync.SyncData.ExerciseChange;
import com.gym.api.sync.SyncData.FoodChange;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class SyncService {

    private final JdbcTemplate jdbc;

    public SyncService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public SyncData sync(Long userId, SyncData in) {
        for (ExerciseChange e : orEmpty(in.exercises())) pushExercise(userId, e);
        for (EntryChange e : orEmpty(in.entries())) pushEntry(userId, e);
        for (BodyWeightChange b : orEmpty(in.bodyWeights())) pushBodyWeight(userId, b);
        for (FoodChange f : orEmpty(in.foods())) pushFood(userId, f);
        if (in.profile() != null) pushProfile(userId, in.profile());
        return pull(userId, in.cursor() == null ? 0 : in.cursor());
    }

    private void pushExercise(Long userId, ExerciseChange e) {
        if (Boolean.TRUE.equals(e.deleted())) {
            jdbc.update("UPDATE exercise SET deleted = true WHERE uuid = ? AND user_id = ? AND NOT deleted",
                    e.uuid(), userId);
            return;
        }
        require(e.name() != null && !e.name().isBlank(), "У упражнения нет названия");
        String name = freeName(userId, e.uuid(), e.name().trim());
        jdbc.update("""
                INSERT INTO exercise (uuid, user_id, name, favorite) VALUES (?, ?, ?, ?)
                ON CONFLICT (uuid) DO UPDATE SET name = EXCLUDED.name, favorite = EXCLUDED.favorite, deleted = false
                WHERE exercise.user_id = EXCLUDED.user_id
                """, e.uuid(), userId, name, e.favorite() == null ? 0 : e.favorite());
    }

    private String freeName(Long userId, String uuid, String name) {
        String candidate = name;
        int n = 2;
        while (Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM exercise WHERE user_id = ? AND name = ? AND uuid <> ? AND NOT deleted)",
                Boolean.class, userId, candidate, uuid))) {
            String suffix = " (" + n++ + ")";
            candidate = name.substring(0, Math.min(name.length(), 100 - suffix.length())) + suffix;
        }
        return candidate;
    }

    private void pushEntry(Long userId, EntryChange e) {
        if (Boolean.TRUE.equals(e.deleted())) {
            jdbc.update("""
                    UPDATE entry SET deleted = true WHERE uuid = ? AND NOT deleted
                    AND exercise_id IN (SELECT id FROM exercise WHERE user_id = ?)
                    """, e.uuid(), userId);
            return;
        }
        require(e.exerciseUuid() != null && e.date() != null && e.weight() != null && e.reps() != null && e.reps() > 0, "У подхода нет упражнения, даты или повторов");
        List<Long> exercise = jdbc.queryForList(
                "SELECT id FROM exercise WHERE uuid = ? AND user_id = ? AND NOT deleted",
                Long.class, e.exerciseUuid(), userId);
        if (exercise.isEmpty()) return;
        jdbc.update("""
                INSERT INTO entry (uuid, exercise_id, date, weight, reps) VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (uuid) DO UPDATE SET exercise_id = EXCLUDED.exercise_id, date = EXCLUDED.date,
                    weight = EXCLUDED.weight, reps = EXCLUDED.reps, deleted = false
                WHERE entry.exercise_id IN (SELECT id FROM exercise WHERE user_id = ?)
                """, e.uuid(), exercise.get(0), e.date(), e.weight(), e.reps(), userId);
    }

    private void pushBodyWeight(Long userId, BodyWeightChange b) {
        if (Boolean.TRUE.equals(b.deleted())) {
            jdbc.update("UPDATE body_weight SET deleted = true WHERE user_id = ? AND date = ? AND NOT deleted",
                    userId, b.date());
            return;
        }
        require(b.weight() != null && b.weight() > 0, "Вес должен быть больше нуля");
        int updated = jdbc.update("UPDATE body_weight SET weight = ? WHERE user_id = ? AND date = ? AND NOT deleted",
                b.weight(), userId, b.date());
        if (updated == 0) {
            jdbc.update("INSERT INTO body_weight (user_id, date, weight) VALUES (?, ?, ?)", userId, b.date(), b.weight());
        }
    }

    private void pushFood(Long userId, FoodChange f) {
        if (Boolean.TRUE.equals(f.deleted())) {
            jdbc.update("UPDATE food SET deleted = true WHERE uuid = ? AND user_id = ? AND NOT deleted", f.uuid(), userId);
            return;
        }
        require(f.date() != null && f.name() != null && !f.name().isBlank() && f.grams() != null && f.grams() > 0 && f.kcal() != null && f.protein() != null && f.fat() != null && f.carbs() != null,
                "У еды нет даты, названия или веса");
        jdbc.update("""
                INSERT INTO food (uuid, user_id, date, name, grams, kcal, protein, fat, carbs, code)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (uuid) DO UPDATE SET date = EXCLUDED.date, name = EXCLUDED.name, grams = EXCLUDED.grams,
                    kcal = EXCLUDED.kcal, protein = EXCLUDED.protein, fat = EXCLUDED.fat, carbs = EXCLUDED.carbs,
                    code = EXCLUDED.code, deleted = false
                WHERE food.user_id = EXCLUDED.user_id
                """, f.uuid(), userId, f.date(), f.name().trim(), f.grams(), f.kcal(), f.protein(), f.fat(),
                f.carbs(), f.code());
    }

    private void pushProfile(Long userId, ProfileDto p) {
        jdbc.update("""
                INSERT INTO profile (user_id, name, sex, age, height, target, goal, experience, per_week, priorities)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET name = EXCLUDED.name, sex = EXCLUDED.sex, age = EXCLUDED.age,
                    height = EXCLUDED.height, target = EXCLUDED.target, goal = EXCLUDED.goal,
                    experience = EXCLUDED.experience, per_week = EXCLUDED.per_week, priorities = EXCLUDED.priorities
                """, userId, p.name() == null ? "" : p.name().trim(), p.sex(), p.age(), p.height(), p.target(),
                p.goal(), p.experience(), p.perWeek(), p.priorities() == null ? "" : p.priorities());
    }

    private SyncData pull(Long userId, long since) {
        long[] cursor = {since};
        List<ExerciseChange> exercises = jdbc.query(
                "SELECT uuid, name, favorite, deleted, version FROM exercise WHERE user_id = ? AND version > ? ORDER BY version",
                (rs, i) -> {
                    cursor[0] = Math.max(cursor[0], rs.getLong("version"));
                    return new ExerciseChange(rs.getString("uuid"), rs.getString("name"), rs.getInt("favorite"),
                            rs.getBoolean("deleted"));
                }, userId, since);
        List<EntryChange> entries = jdbc.query("""
                        SELECT en.uuid, e.uuid AS exercise_uuid, en.date, en.weight, en.reps, en.deleted, en.version
                        FROM entry en JOIN exercise e ON e.id = en.exercise_id
                        WHERE e.user_id = ? AND en.version > ? ORDER BY en.version
                        """,
                (rs, i) -> {
                    cursor[0] = Math.max(cursor[0], rs.getLong("version"));
                    return new EntryChange(rs.getString("uuid"), rs.getString("exercise_uuid"),
                            rs.getObject("date", LocalDate.class), rs.getDouble("weight"), rs.getInt("reps"),
                            rs.getBoolean("deleted"));
                }, userId, since);
        List<BodyWeightChange> bodyWeights = jdbc.query(
                "SELECT date, weight, deleted, version FROM body_weight WHERE user_id = ? AND version > ? ORDER BY version",
                (rs, i) -> {
                    cursor[0] = Math.max(cursor[0], rs.getLong("version"));
                    return new BodyWeightChange(rs.getObject("date", LocalDate.class), rs.getDouble("weight"),
                            rs.getBoolean("deleted"));
                }, userId, since);
        List<FoodChange> foods = jdbc.query("""
                        SELECT uuid, date, name, grams, kcal, protein, fat, carbs, code, deleted, version
                        FROM food WHERE user_id = ? AND version > ? ORDER BY version
                        """,
                (rs, i) -> {
                    cursor[0] = Math.max(cursor[0], rs.getLong("version"));
                    return new FoodChange(rs.getString("uuid"), rs.getObject("date", LocalDate.class),
                            rs.getString("name"), rs.getDouble("grams"), rs.getDouble("kcal"), rs.getDouble("protein"),
                            rs.getDouble("fat"), rs.getDouble("carbs"), rs.getString("code"), rs.getBoolean("deleted"));
                }, userId, since);
        List<ProfileDto> profile = jdbc.query("""
                        SELECT name, sex, age, height, target, goal, experience, per_week, priorities, version
                        FROM profile WHERE user_id = ? AND version > ?
                        """,
                (rs, i) -> {
                    cursor[0] = Math.max(cursor[0], rs.getLong("version"));
                    return new ProfileDto(rs.getString("name"), rs.getString("sex"), rs.getInt("age"),
                            rs.getInt("height"), rs.getDouble("target"), rs.getString("goal"),
                            rs.getString("experience"), rs.getInt("per_week"), rs.getString("priorities"));
                }, userId, since);
        return new SyncData(cursor[0], exercises, entries, bodyWeights, foods, profile.isEmpty() ? null : profile.get(0));
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
