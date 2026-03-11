package com.learn.knowledgesystem.service;

import com.learn.knowledgesystem.dto.NoteDTO;
import com.learn.knowledgesystem.dto.TagDTO;
import com.learn.knowledgesystem.entity.Note;
import com.learn.knowledgesystem.entity.Tag;
import com.learn.knowledgesystem.entity.User;
import com.learn.knowledgesystem.repository.NoteRepository;
import com.learn.knowledgesystem.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagService tagService;

    // 根据 ID 获取单个笔记的 DTO
    @Transactional(readOnly = true)
    public NoteDTO getNoteDTOById(Long id) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return null;
        Map<Long, Set<TagDTO>> tagsMap = fetchTagsForNotes(Collections.singletonList(id));
        return buildNoteDTO(note, tagsMap.getOrDefault(id, new HashSet<>()));
    }

    // 根据关键词搜索笔记并返回 DTO 列表
    @Transactional(readOnly = true)
    public List<NoteDTO> searchNoteDTOs(Long userId, String keyword) {
        List<Note> notes;
        if (keyword == null || keyword.trim().isEmpty()) {
            notes = noteRepository.findByUserId(userId);
        } else {
            notes = noteRepository.searchByUserIdAndKeyword(userId, keyword.trim());
        }
        if (notes.isEmpty()) return new ArrayList<>();
        List<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toList());
        Map<Long, Set<TagDTO>> tagsMap = fetchTagsForNotes(noteIds);
        return notes.stream()
                .map(note -> buildNoteDTO(note, tagsMap.getOrDefault(note.getId(), new HashSet<>())))
                .collect(Collectors.toList());
    }

    //  根据用户ID和标签ID查询笔记实体
    @Transactional(readOnly = true)
    public List<NoteDTO> getNoteDTOsByTag(Long userId, Long tagId) {
        List<Note> notes = noteRepository.findByUserIdAndTagId(userId, tagId);
        if (notes.isEmpty()) return new ArrayList<>();

        // 收集笔记ID，用于一次性加载标签（你的服务中已有 fetchTagsForNotes 方法）
        List<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toList());
        Map<Long, Set<TagDTO>> tagsMap = fetchTagsForNotes(noteIds);

        // 转换为 DTO
        return notes.stream()
                .map(note -> buildNoteDTO(note, tagsMap.getOrDefault(note.getId(), new HashSet<>())))
                .collect(Collectors.toList());
    }

    // 获取笔记列表
    @Transactional(readOnly = true)
    public List<NoteDTO> getNoteDTOsByUser(Long userId) {
        List<Note> notes = noteRepository.findByUserId(userId);
        if (notes.isEmpty()) return new ArrayList<>();
        List<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toList());
        Map<Long, Set<TagDTO>> tagsMap = fetchTagsForNotes(noteIds);
        return notes.stream()
                .map(note -> buildNoteDTO(note, tagsMap.getOrDefault(note.getId(), new HashSet<>())))
                .collect(Collectors.toList());
    }

    // 构建单个 NoteDTO
    private NoteDTO buildNoteDTO(Note note, Set<TagDTO> tags) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setTags(tags);
        return dto;
    }

    // 原生 SQL 查询笔记-标签映射
    private Map<Long, Set<TagDTO>> fetchTagsForNotes(List<Long> noteIds) {
        if (noteIds.isEmpty()) return new HashMap<>();
        String sql = "SELECT nt.note_id, t.id, t.name FROM note_tag nt JOIN tags t ON nt.tag_id = t.id WHERE nt.note_id IN (:noteIds)";
        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("noteIds", noteIds)
                .getResultList();

        Map<Long, Set<TagDTO>> tagsMap = new HashMap<>();
        for (Object[] row : results) {
            Long noteId = ((Number) row[0]).longValue();
            Long tagId = ((Number) row[1]).longValue();
            String tagName = (String) row[2];

            TagDTO tagDTO = new TagDTO();
            tagDTO.setId(tagId);
            tagDTO.setName(tagName);

            tagsMap.computeIfAbsent(noteId, k -> new HashSet<>()).add(tagDTO);
        }
        return tagsMap;
    }

    // 创建笔记
    @Transactional
    public Note createNote(Note note, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        note.setId(null);
        note.setUser(user);
        Note savedNote = noteRepository.save(note);

        Set<Long> tagIds = processTags(note.getTags());
        updateNoteTags(savedNote.getId(), tagIds);

        return savedNote;
    }

    // 更新笔记
    @Transactional
    public Note updateNote(Note note) {
        Note existing = noteRepository.findById(note.getId())
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        existing.setTitle(note.getTitle());
        existing.setContent(note.getContent());

        Set<Long> tagIds = processTags(note.getTags());
        updateNoteTags(existing.getId(), tagIds);

        return existing;
    }


    // updateNoteTags 方法，接收 Set<Long>
    private void updateNoteTags(Long noteId, Set<Long> tagIds) {
        // 1. 删除所有现有关联
        entityManager.createNativeQuery("DELETE FROM note_tag WHERE note_id = ?")
                .setParameter(1, noteId)
                .executeUpdate();

        // 2. 插入新关联
        for (Long tagId : tagIds) {
            entityManager.createNativeQuery("INSERT INTO note_tag (note_id, tag_id) VALUES (?, ?)")
                    .setParameter(1, noteId)
                    .setParameter(2, tagId)
                    .executeUpdate();
        }
    }

    // 删除笔记
    @Transactional
    public void deleteNote(Long id) {
        // 先删除关联，再删除笔记
        entityManager.createNativeQuery("DELETE FROM note_tag WHERE note_id = ?")
                .setParameter(1, id)
                .executeUpdate();
        noteRepository.deleteById(id);
    }

    // processTags 方法，返回 Set<Long> 而不是 Set<Tag>
    private Set<Long> processTags(Set<Tag> tags) {
        Set<Long> tagIds = new HashSet<>();
        if (tags == null) return tagIds;
        for (Tag tag : tags) {
            if (tag.getId() != null) {
                // 验证该 ID 是否存在（可选）
                tagService.getTagById(tag.getId()).ifPresent(t -> tagIds.add(t.getId()));
            } else if (tag.getName() != null && !tag.getName().trim().isEmpty()) {
                Tag existing = tagService.findOrCreateTag(tag.getName().trim());
                tagIds.add(existing.getId());
            }
        }
        return tagIds;
    }

    // 生成分享 ID
    @Transactional
    public String generateShareId(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));
        if (!note.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        if (note.getShareId() == null) {
            note.setShareId(UUID.randomUUID().toString());
            noteRepository.save(note);
        }
        return note.getShareId();
    }
}