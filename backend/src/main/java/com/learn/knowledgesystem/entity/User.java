package com.learn.knowledgesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

//用户实体
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)   //名字不能为空
    private String username;


    @Column(nullable = false)     //密码不能为空
    private String password;

    private String email;
    private String nickname;

    @Column(name = "created_at")
    private LocalDateTime createdAt;     //什么时间被创建

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;     //什么时间被更新

    private String avatarUrl; // 头像URL


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
