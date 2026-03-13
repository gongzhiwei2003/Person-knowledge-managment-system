package com.learn.knowledgesystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.learn.knowledgesystem.entity.LearningGoal;
import com.learn.knowledgesystem.entity.Roadmap;
import com.learn.knowledgesystem.entity.User;
import com.learn.knowledgesystem.repository.LearningGoalRepository;
import com.learn.knowledgesystem.repository.RoadmapRepository;
import com.learn.knowledgesystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RoadmapService {

    private static final Logger logger = LoggerFactory.getLogger(RoadmapService.class);

    @Autowired
    private RoadmapRepository roadmapRepository;
    @Autowired
    private LearningGoalRepository learningGoalRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    public Roadmap createRoadmap(Roadmap roadmap, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        roadmap.setId(null);
        roadmap.setUser(user);
        return roadmapRepository.save(roadmap);
    }

    public Roadmap updateRoadmapGraph(Long id, Long userId, String graphData) {
        Roadmap roadmap = roadmapRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new RuntimeException("路线图不存在或无权限"));
        roadmap.setGraphData(graphData);
        return roadmapRepository.save(roadmap);
    }

    public Roadmap getRoadmap(Long id, Long userId) {
        return roadmapRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new RuntimeException("路线图不存在"));
    }

    public List<Roadmap> getUserRoadmaps(Long userId) {
        return roadmapRepository.findByUserId(userId);
    }

    public void deleteRoadmap(Long id, Long userId) {
        Roadmap roadmap = roadmapRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new RuntimeException("路线图不存在"));
        roadmapRepository.delete(roadmap);
    }

    @Transactional
    public void updateNodeCompletion(Long userId, Long roadmapId, String nodeId, boolean completed) {
        logger.info("更新节点状态：roadmapId={}, nodeId={}, completed={}", roadmapId, nodeId, completed);
        Roadmap roadmap = roadmapRepository.findByUserIdAndId(userId, roadmapId)
                .orElseThrow(() -> new RuntimeException("路线图不存在或无权限"));

        try {
            JsonNode root = objectMapper.readTree(roadmap.getGraphData());
            JsonNode nodes = root.get("nodes");
            if (nodes != null && nodes.isArray()) {
                for (JsonNode node : nodes) {
                    if (node.has("id") && node.get("id").asText().equals(nodeId)) {
                        ((ObjectNode) node).put("completed", completed);
                        break;
                    }
                }
            }
            roadmap.setGraphData(objectMapper.writeValueAsString(root));
            roadmapRepository.save(roadmap);

            // 更新所有关联该路线图的学习目标进度
            updateGoalsProgressByRoadmap(roadmap);
        } catch (Exception e) {
            throw new RuntimeException("更新节点状态失败", e);
        }
    }

    private void updateGoalsProgressByRoadmap(Roadmap roadmap) {
        int totalNodes = 0;
        int completedNodes = 0;
        try {
            JsonNode root = objectMapper.readTree(roadmap.getGraphData());
            JsonNode nodes = root.get("nodes");
            if (nodes != null && nodes.isArray()) {
                for (JsonNode node : nodes) {
                    totalNodes++;
                    if (node.has("completed") && node.get("completed").asBoolean()) {
                        completedNodes++;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析路线图 graphData 失败", e);
            return;
        }

        int progress = totalNodes == 0 ? 0 : (completedNodes * 100 / totalNodes);

        List<LearningGoal> goals = learningGoalRepository.findByRoadmapId(roadmap.getId());
        for (LearningGoal goal : goals) {
            goal.setProgress(progress);
            if (progress == 100) {
                goal.setStatus(LearningGoal.GoalStatus.COMPLETED);
            } else if (goal.getStatus() == LearningGoal.GoalStatus.COMPLETED) {
                goal.setStatus(LearningGoal.GoalStatus.IN_PROGRESS);
            }
            learningGoalRepository.save(goal);
        }
    }

    @Transactional
    public String generateShareId(Long roadmapId, Long userId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new RuntimeException("路线图不存在"));
        if (!roadmap.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限");
        }
        if (roadmap.getShareId() == null) {
            roadmap.setShareId(UUID.randomUUID().toString());
            roadmapRepository.save(roadmap);
        }
        return roadmap.getShareId();
    }


}