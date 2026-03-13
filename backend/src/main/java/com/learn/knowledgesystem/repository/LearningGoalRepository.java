package com.learn.knowledgesystem.repository;

import com.learn.knowledgesystem.entity.LearningGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LearningGoalRepository extends JpaRepository<LearningGoal, Long> {
    List<LearningGoal> findByUserId(Long userId);
    List<LearningGoal> findByRoadmapId(Long roadmapId);
}