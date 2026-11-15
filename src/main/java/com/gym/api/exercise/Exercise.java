package com.gym.api.exercise;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@SQLDelete(sql = "UPDATE exercise SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid = UUID.randomUUID().toString();

    private Long userId;

    private String name;

    private int favorite;

    protected Exercise() {
    }

    public Exercise(Long userId, String name, int favorite) {
        this.userId = userId;
        this.name = name;
        this.favorite = favorite;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}
