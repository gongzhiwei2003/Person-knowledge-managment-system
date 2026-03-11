package com.learn.knowledgesystem.service;

import com.learn.knowledgesystem.dto.StatisticsDTO;
import com.learn.knowledgesystem.entity.LearningGoal;
import com.learn.knowledgesystem.entity.Note;
import com.learn.knowledgesystem.entity.Tag;
import com.learn.knowledgesystem.repository.LearningGoalRepository;
import com.learn.knowledgesystem.repository.NoteRepository;
import com.learn.knowledgesystem.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LearningGoalRepository goalRepository;

    public StatisticsDTO getUserStatistics(Long userId) {
        StatisticsDTO dto = new StatisticsDTO();

        // 1. 笔记趋势（近6个月）
        List<Note> notes = noteRepository.findByUserId(userId);
        Map<String, Long> noteTrend = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            String month = now.minusMonths(i).format(formatter);
            noteTrend.put(month, 0L);
        }
        for (Note note : notes) {
            String month = note.getCreatedAt().format(formatter);
            noteTrend.merge(month, 1L, Long::sum);
        }
        List<StatisticsDTO.NoteTrendDTO> trendList = noteTrend.entrySet().stream()
                .map(e -> {
                    StatisticsDTO.NoteTrendDTO d = new StatisticsDTO.NoteTrendDTO();
                    d.setMonth(e.getKey());
                    d.setCount(e.getValue());
                    return d;
                })
                .collect(Collectors.toList());
        dto.setNoteTrend(trendList);

        // 2. 标签统计（前10个）
        List<Tag> tags = tagRepository.findAll(); // 实际应只统计当前用户的标签，但标签是全局的，可按笔记关联统计
        Map<String, Long> tagCount = new HashMap<>();
        for (Tag tag : tags) {
            long count = noteRepository.findNotesByTagId(tag.getId()).size();
            tagCount.put(tag.getName(), count);
        }
        List<StatisticsDTO.TagCountDTO> tagCounts = tagCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    StatisticsDTO.TagCountDTO d = new StatisticsDTO.TagCountDTO();
                    d.setName(e.getKey());
                    d.setCount(e.getValue());
                    return d;
                })
                .collect(Collectors.toList());
        dto.setTagCounts(tagCounts);

        // 3. 目标进度
        List<LearningGoal> goals = goalRepository.findByUserId(userId);
        long total = goals.size();
        long completed = goals.stream().filter(g -> g.getStatus() == LearningGoal.GoalStatus.COMPLETED).count();
        StatisticsDTO.GoalProgressDTO goalProgress = new StatisticsDTO.GoalProgressDTO();
        goalProgress.setTotalGoals(total);
        goalProgress.setCompletedGoals(completed);
        goalProgress.setCompletionRate(total == 0 ? 0 : (double) completed / total);
        dto.setGoalProgress(goalProgress);

        return dto;
    }
}