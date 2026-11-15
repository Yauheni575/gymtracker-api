package com.gym.api.entry;

import com.gym.api.exercise.Exercise;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@SQLDelete(sql = "UPDATE entry SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    private LocalDate date;

    private double weight;

    private int reps;

    protected Entry() {
    }

    public Entry(Exercise exercise, LocalDate date, double weight, int reps) {
        this.exercise = exercise;
        this.date = date;
        this.weight = weight;
        this.reps = reps;
    }

    public Long getId() {
        return id;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }

    public int getReps() {
        return reps;
    }
}
