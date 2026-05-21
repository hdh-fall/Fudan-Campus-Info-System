package com.fudan.campusinfo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "facility")
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_id")
    private Integer facilityId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "open_time", length = 100)
    private String openTime;

    @Column(name = "location_desc", length = 200)
    private String locationDesc;

    @Column
    private Integer capacity;

    @Column(length = 50)
    private String contact;

    @Column(columnDefinition = "TEXT")
    private String description;
}
