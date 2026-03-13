package com.learn.knowledgesystem.service;
import com.learn.knowledgesystem.dto.AttachmentDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



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

    @Value("${app.upload.image-dir}")
    private String imageDir;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.upload.file-dir}")
    private String fileDir;

    private String extractFileNameFromDownloadUrl(String downloadUrl) {
        return downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
    }

    // 保存笔记时，将 attachments 列表转为 JSON 字符串
    private String serializeAttachments(List<AttachmentDTO> attachments) {
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (Exception e) {
            return "[]";
        }
    }

    // 读取笔记时，将 JSON 字符串转为列表
    private List<AttachmentDTO> deserializeAttachments(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<AttachmentDTO>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    /**
     * 从笔记内容中提取所有图片URL，并删除对应的物理文件
     * @param content 笔记HTML内容
     */
    private void deleteImagesFromContent(String content) {
        if (content == null || content.isEmpty()) return;
        // 匹配 <img src="/uploads/images/xxx.png"> 中的URL
        Pattern pattern = Pattern.compile("<img\\s+[^>]*src=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String url = matcher.group(1);
            // 提取文件名（例如 /uploads/images/xxx.png -> xxx.png）
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            Path filePath = Paths.get(imageDir, fileName);
            try {
                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    System.out.println("已删除图片文件: " + filePath);
                }
            } catch (IOException e) {
                System.err.println("删除图片文件失败: " + filePath + ", 错误: " + e.getMessage());
            }
        }
    }

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
        // 解析 attachments JSON
        if (note.getAttachments() != null && !note.getAttachments().isEmpty()) {
            dto.setAttachments(deserializeAttachments(note.getAttachments()));
        } else {
            dto.setAttachments(new ArrayList<>());
        }
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

    private void deleteAttachmentsFiles(String attachmentsJson) {
        List<AttachmentDTO> attachments = deserializeAttachments(attachmentsJson);
        for (AttachmentDTO att : attachments) {
            if ("file".equals(att.getType()) || "video".equals(att.getType())) {
                deleteAttachmentFile(att);
            }
        }
    }

    private void deleteAttachmentFile(AttachmentDTO att) {
        String url = att.getUrl();
        String fileName = extractFileNameFromDownloadUrl(url);
        Path filePath = null;
        if (url.contains("/images/")) {
            filePath = Paths.get(imageDir, fileName);
        } else if (url.contains("/files/")) {
            filePath = Paths.get(fileDir, fileName);
        }
        if (filePath != null) {
            try {
                Files.deleteIfExists(filePath);
                System.out.println("已删除附件文件: " + filePath);
            } catch (IOException e) {
                System.err.println("删除附件文件失败: " + filePath);
            }
        }
    }

    // 更新笔记
    @Transactional
    public Note updateNote(Note note) {
        Note existing = noteRepository.findById(note.getId())
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        // 获取旧内容中的图片URL（用于后续清理）
        String oldContent = existing.getContent();

        existing.setTitle(note.getTitle());
        existing.setContent(note.getContent());
        existing.setAttachments(note.getAttachments()); // 更新附件 JSON

        Set<Long> tagIds = processTags(note.getTags());
        updateNoteTags(existing.getId(), tagIds);

        // 保存笔记（必须先保存，因为旧内容需要保留到对比完成）
        Note saved = noteRepository.save(existing);

        // 清理旧内容中已不再使用的图片
        if (oldContent != null) {
            // 提取旧内容中的图片URL
            List<String> oldImages = extractImageUrls(oldContent);
            List<String> newImages = extractImageUrls(note.getContent());
            // 找出需要删除的图片：旧内容中有，新内容中没有
            oldImages.removeAll(newImages);
            for (String url : oldImages) {
                String fileName = url.substring(url.lastIndexOf('/') + 1);
                Path filePath = Paths.get(imageDir, fileName);
                try {
                    Files.deleteIfExists(filePath);
                    System.out.println("已删除旧图片: " + filePath);
                } catch (IOException e) {
                    System.err.println("删除旧图片失败: " + filePath + ", " + e.getMessage());
                }
            }
        }

        // 获取旧附件列表
        String oldAttachmentsJson = existing.getAttachments();
        List<AttachmentDTO> oldAttachments = deserializeAttachments(oldAttachmentsJson);
        List<AttachmentDTO> newAttachments = deserializeAttachments(note.getAttachments());

// 找出被移除的附件
        for (AttachmentDTO oldAtt : oldAttachments) {
            if (oldAtt.getType().equals("file") || oldAtt.getType().equals("video")) {
                boolean stillExists = newAttachments.stream()
                        .anyMatch(newAtt -> newAtt.getUrl().equals(oldAtt.getUrl()));
                if (!stillExists) {
                    // 删除文件
                    deleteAttachmentFile(oldAtt);
                }
            }
        }

        return saved;
    }

    /**
     * 从HTML内容中提取所有图片URL
     */
    private List<String> extractImageUrls(String content) {
        List<String> urls = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img\\s+[^>]*src=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
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
        Note note = noteRepository.findById(id).orElse(null);
        if (note != null) {
            // 删除笔记内容中的所有图片
            deleteImagesFromContent(note.getContent());
            // 删除附件文件（非链接）
            deleteAttachmentsFiles(note.getAttachments());
        }
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