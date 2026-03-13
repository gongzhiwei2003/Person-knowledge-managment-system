package com.learn.knowledgesystem.service;

import com.learn.knowledgesystem.entity.LearningGoal;
import com.learn.knowledgesystem.entity.Roadmap;
import com.learn.knowledgesystem.entity.User;
import com.learn.knowledgesystem.repository.LearningGoalRepository;
import com.learn.knowledgesystem.repository.RoadmapRepository;
import com.learn.knowledgesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class LearningGoalService {

    @Autowired
    private LearningGoalRepository goalRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private UserRepository userRepository;

    public List<LearningGoal> getGoalsByUser(Long userId) {
        return goalRepository.findByUserId(userId);
    }

    public LearningGoal getGoalById(Long id) {
        return goalRepository.findById(id).orElse(null);
    }

    @Transactional
    public LearningGoal createGoal(LearningGoal goal, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        goal.setId(null);
        goal.setUser(user);
        goal.setProgress(0);
        goal.setStatus(LearningGoal.GoalStatus.PLANNING);

        // 先保存目标（此时尚未关联路线图）
        LearningGoal savedGoal = goalRepository.save(goal);

        // 如果新建目标时指定了关联路线图，则更新路线图的 goal 字段
        Long roadmapId = goal.getRoadmapId(); // 注意：这里的 goal 是传入的，可能包含 roadmapId
        if (roadmapId != null) {
            Roadmap roadmap = roadmapRepository.findById(roadmapId)
                    .orElseThrow(() -> new RuntimeException("路线图不存在"));
            roadmap.setGoal(savedGoal); // 设置双向关联
            roadmapRepository.save(roadmap);

            // 同时更新目标的 roadmapId（可选，但保存时已设置，无需再操作）
            // savedGoal.setRoadmapId(roadmapId); // 已在 goal 中设置，且保存时已存
        }
        return savedGoal;
    }

    @Transactional
    public LearningGoal updateGoal(LearningGoal goal) {
        LearningGoal existing = goalRepository.findById(goal.getId())
                .orElseThrow(() -> new RuntimeException("目标不存在"));

        // 如果 roadmapId 发生了变化，需要处理关联的路线图
        Long oldRoadmapId = existing.getRoadmapId();
        Long newRoadmapId = goal.getRoadmapId();

        // 更新目标的其他字段
        existing.setTitle(goal.getTitle());
        existing.setDescription(goal.getDescription());
        existing.setStartDate(goal.getStartDate());
        existing.setTargetDate(goal.getTargetDate());
        existing.setStatus(goal.getStatus());
        existing.setProgress(goal.getProgress());

        // 处理路线图关联
        if (!Objects.equals(oldRoadmapId, newRoadmapId)) {
            // 如果原来关联了路线图，清除原路线图的 goal 引用
            if (oldRoadmapId != null) {
                Roadmap oldRoadmap = roadmapRepository.findById(oldRoadmapId).orElse(null);
                if (oldRoadmap != null) {
                    oldRoadmap.setGoal(null);
                    roadmapRepository.save(oldRoadmap);
                }
            }
            // 如果新路线图不为空，设置新路线图的 goal 为当前目标
            if (newRoadmapId != null) {
                Roadmap newRoadmap = roadmapRepository.findById(newRoadmapId)
                        .orElseThrow(() -> new RuntimeException("路线图不存在"));
                newRoadmap.setGoal(existing);
                roadmapRepository.save(newRoadmap);
            }
            existing.setRoadmapId(newRoadmapId);
        }

        return goalRepository.save(existing);
    }

    @Transactional
    public void deleteGoal(Long id) {
        goalRepository.deleteById(id);
    }
}