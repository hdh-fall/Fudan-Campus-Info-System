package com.fudan.campusinfo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Time;

@Data
@Entity
@Table(name = "building")
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "building_id")
    private Integer buildingId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String type;

    @ManyToOne
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @Column
    private Integer floors;

    @Column(name = "open_time")
    private Time openTime;

    @Column(name = "close_time")
    private Time closeTime;

    @Column(columnDefinition = "TEXT")
    private String description;
}
