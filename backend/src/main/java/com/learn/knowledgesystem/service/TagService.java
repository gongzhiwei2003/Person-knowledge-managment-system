package com.learn.knowledgesystem.service;

import com.learn.knowledgesystem.dto.TagDTO;
import com.learn.knowledgesystem.entity.Note;
import com.learn.knowledgesystem.entity.Tag;
import com.learn.knowledgesystem.repository.NoteRepository;
import com.learn.knowledgesystem.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NoteRepository noteRepository;  // 用于解除关联

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    public Optional<Tag> findByName(String name) {
        return Optional.ofNullable(tagRepository.findByName(name));
    }

    @Transactional
    public Tag createTag(String name) {
        // 检查是否已存在同名标签（避免唯一约束异常）
        return findByName(name).orElseGet(() -> {
            Tag tag = new Tag();
            tag.setName(name);
            return tagRepository.save(tag);
        });
    }

    @Transactional
    public Tag updateTag(Long id, String name) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new RuntimeException("标签不存在"));
        tag.setName(name);
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签不存在，id: " + id));

        // 解除所有笔记对该标签的引用
        List<Note> notesWithTag = noteRepository.findNotesByTagId(id); // 修改此处方法名
        for (Note note : notesWithTag) {
            note.getTags().remove(tag);
            noteRepository.save(note);
        }

        tagRepository.delete(tag);
    }

    @Transactional
    public Tag findOrCreateTag(String name) {
        return findByName(name).orElseGet(() -> createTag(name));
    }


    public List<TagDTO> getAllTagDTOs() {
        return tagRepository.findAll()
                .stream()
                .map(tag -> {
                    TagDTO dto = new TagDTO();
                    dto.setId(tag.getId());
                    dto.setName(tag.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

}