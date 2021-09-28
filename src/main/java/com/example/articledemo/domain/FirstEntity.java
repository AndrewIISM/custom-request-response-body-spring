package com.example.articledemo.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "first_entity")
public class FirstEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    public FirstEntity() {
    }

    public FirstEntity(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "FirstEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }

}
