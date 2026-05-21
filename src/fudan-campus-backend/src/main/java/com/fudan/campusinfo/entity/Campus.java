package com.fudan.campusinfo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "campus")
public class Campus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campus_id")
    private Integer campusId;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String address;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String description;
}
