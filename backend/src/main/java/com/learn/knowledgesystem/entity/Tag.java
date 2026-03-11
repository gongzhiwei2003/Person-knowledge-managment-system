package com.learn.knowledgesystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

   //mappedBy = "tags" 表示 Tag 是关系的反方，
   // 关联由 Note 实体中的 tags 字段维护。这样可以避免生成中间表的外键约束冲突。
   @ManyToMany(mappedBy = "tags")
   @JsonIgnore   // 防止序列化时加载笔记
   private Set<Note> notes = new HashSet<>();

}