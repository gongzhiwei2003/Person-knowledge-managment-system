package com.learn.knowledgesystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SharedRoadmapDTO {
    private Long id;
    private String title;
    private String description;
    private String graphData; // 包含节点和边的 JSON
    private LocalDateTime createdAt;
    // 不包含用户信息
}