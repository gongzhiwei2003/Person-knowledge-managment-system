package com.learn.knowledgesystem.controller;

import com.learn.knowledgesystem.common.ApiResponse;
import com.learn.knowledgesystem.dto.NoteDTO;
import com.learn.knowledgesystem.entity.Note;
import com.learn.knowledgesystem.service.NoteService;
import com.learn.knowledgesystem.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping
    public ApiResponse<List<NoteDTO>> getUserNotes(@RequestParam(required = false) Long tagId) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<NoteDTO> notes;
        if (tagId != null) {
            notes = noteService.getNoteDTOsByTag(userId, tagId);
        } else {
            notes = noteService.getNoteDTOsByUser(userId);
        }
        return ApiResponse.success(notes);
    }

    @GetMapping("/{id}")
    public ApiResponse<NoteDTO> getNote(@PathVariable Long id) {
        NoteDTO note = noteService.getNoteDTOById(id);
        if (note == null) {
            return ApiResponse.error(404, "笔记不存在");
        }
        return ApiResponse.success(note);
    }

    @GetMapping("/search")
    public ApiResponse<List<NoteDTO>> searchNotes(@RequestParam String keyword) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<NoteDTO> notes = noteService.searchNoteDTOs(userId, keyword);
        return ApiResponse.success(notes);
    }

    @PostMapping("/{id}/share")
    public ApiResponse<String> generateNoteShareId(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        String shareId = noteService.generateShareId(id, userId);
        return ApiResponse.success(shareId);
    }

    @PostMapping
    public ApiResponse<Note> createNote(@RequestBody Note note) {
        Long userId = SecurityUtil.getCurrentUserId();
        Note saved = noteService.createNote(note, userId);
        return ApiResponse.success("创建成功", saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<Note> updateNote(@PathVariable Long id, @RequestBody Note note) {
        note.setId(id);
        Note updated = noteService.updateNote(note);
        return ApiResponse.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteNote(@PathVariable Long id) {
        System.out.println("进入 deleteNote 方法，id = " + id);
        noteService.deleteNote(id);
        return ApiResponse.success("删除成功", null);
    }

}

