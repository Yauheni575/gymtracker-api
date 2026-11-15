package com.gym.api.bodyweight;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@SQLDelete(sql = "UPDATE body_weight SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class BodyWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate date;

    private double weight;

    protected BodyWeight() {
    }

    public BodyWeight(Long userId, LocalDate date) {
        this.userId = userId;
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
