package com.gym.api.food;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@SQLDelete(sql = "UPDATE food SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid = UUID.randomUUID().toString();

    private Long userId;
    private LocalDate date;
    private String name;
    private double grams;
    private double kcal;
    private double protein;
    private double fat;
    private double carbs;
    private String code;

    protected Food() {
    }

    public Food(Long userId, FoodRequest r) {
        this.userId = userId;
        this.date = r.date();
        this.name = r.name().trim();
        this.grams = r.grams();
        this.kcal = r.kcal();
        this.protein = r.protein();
        this.fat = r.fat();
        this.carbs = r.carbs();
        this.code = r.code();
    }

    FoodResponse toResponse() {
        return new FoodResponse(id, date, name, grams, kcal, protein, fat, carbs, code);
    }
}
