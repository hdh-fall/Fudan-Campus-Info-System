package com.fudan.campusinfo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Integer eventId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "location_desc", length = 200)
    private String locationDesc;

    @ManyToOne
    @JoinColumn(name = "campus_id")
    private Campus campus;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @Column(length = 100)
    private String organizer;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;
}
