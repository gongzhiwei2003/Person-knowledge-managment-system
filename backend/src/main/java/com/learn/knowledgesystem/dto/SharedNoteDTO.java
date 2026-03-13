package com.learn.knowledgesystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class SharedNoteDTO {
    private Long id;
    private String title;
    private String content;
    private Set<TagDTO> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 注意：不包含用户信息
}