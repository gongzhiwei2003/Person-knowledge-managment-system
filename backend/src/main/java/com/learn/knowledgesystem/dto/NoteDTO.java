// NoteDTO.java（确保完整）
package com.learn.knowledgesystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class NoteDTO {
    private Long id;
    private String title;
    private String content;
    private Set<TagDTO> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

