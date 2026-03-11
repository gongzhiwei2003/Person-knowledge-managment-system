package com.learn.knowledgesystem.controller;

import com.learn.knowledgesystem.common.ApiResponse;
import com.learn.knowledgesystem.dto.SharedNoteDTO;
import com.learn.knowledgesystem.dto.SharedRoadmapDTO;
import com.learn.knowledgesystem.dto.TagDTO;
import com.learn.knowledgesystem.entity.Note;
import com.learn.knowledgesystem.entity.Roadmap;
import com.learn.knowledgesystem.entity.Tag;
import com.learn.knowledgesystem.repository.NoteRepository;
import com.learn.knowledgesystem.repository.RoadmapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/share")
public class ShareController {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @GetMapping("/note/{shareId}")
    public ApiResponse<SharedNoteDTO> getSharedNote(@PathVariable String shareId) {
        Note note = noteRepository.findByShareId(shareId)
                .orElseThrow(() -> new RuntimeException("分享链接无效或已过期"));
        SharedNoteDTO dto = new SharedNoteDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        if (note.getTags() != null) {
            dto.setTags(note.getTags().stream().map(tag -> {
                TagDTO tagDTO = new TagDTO();
                tagDTO.setId(tag.getId());
                tagDTO.setName(tag.getName());
                return tagDTO;
            }).collect(Collectors.toSet()));
        }
        return ApiResponse.success(dto);
    }

    @GetMapping("/roadmap/{shareId}")
    public ApiResponse<SharedRoadmapDTO> getSharedRoadmap(@PathVariable String shareId) {
        Roadmap roadmap = roadmapRepository.findByShareId(shareId)
                .orElseThrow(() -> new RuntimeException("分享链接无效或已过期"));
        SharedRoadmapDTO dto = new SharedRoadmapDTO();
        dto.setId(roadmap.getId());
        dto.setTitle(roadmap.getTitle());
        dto.setDescription(roadmap.getDescription());
        dto.setGraphData(roadmap.getGraphData());
        dto.setCreatedAt(roadmap.getCreatedAt());
        return ApiResponse.success(dto);
    }
}