package com.learn.knowledgesystem.repository;

import com.learn.knowledgesystem.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findByUserId(Long userId);

    Optional<Roadmap> findByUserIdAndId(Long userId, Long id);

    List<Roadmap> findByGoalId(Long goalId); //这个方法用于根据学习目标ID查找所有关联的路线图

    Optional<Roadmap> findByShareId(String shareId);
}
