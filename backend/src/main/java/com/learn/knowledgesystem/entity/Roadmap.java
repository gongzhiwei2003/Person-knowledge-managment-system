package com.learn.knowledgesystem.entity;

import com.learn.knowledgesystem.entity.LearningGoal;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "roadmaps")
@Data
public class Roadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Lob
    @Column(columnDefinition = "JSON")
    private String graphData; // 存储 Vue Flow 的节点和边数据

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "goal_id")
    private LearningGoal goal; // 可选关联目标

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(unique = true)
    private String shareId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}