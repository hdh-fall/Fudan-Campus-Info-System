package com.fudan.campusinfo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "query_record")
public class QueryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "query_time", nullable = false)
    private LocalDateTime queryTime;

    @Column(length = 50)
    private String category;

    @Column(name = "result_summary", length = 200)
    private String resultSummary;

    @Column(name = "used_nl2sql")
    private Boolean usedNl2sql = false;

    @Column(name = "client_ip", length = 45)
    private String clientIp;
}
