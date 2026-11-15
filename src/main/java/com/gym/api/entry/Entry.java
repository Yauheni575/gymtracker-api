package com.gym.api.entry;

import com.gym.api.exercise.Exercise;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
