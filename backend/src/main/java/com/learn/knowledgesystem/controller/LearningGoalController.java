package com.learn.knowledgesystem.controller;

import com.learn.knowledgesystem.common.ApiResponse;
import com.learn.knowledgesystem.entity.LearningGoal;
import com.learn.knowledgesystem.service.LearningGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class LearningGoalController {

    @Autowired
    private LearningGoalService goalService;

    // 辅助方法：获取当前登录用户ID（需要根据你的认证方式修改）
    private Long getCurrentUserId() {
        // 从 SecurityContext 中获取用户信息
        // 这里简化返回1L，实际请从 token 中解析
        return 1L;
    }

    @GetMapping
    public ApiResponse<List<LearningGoal>> getUserGoals() {
        Long userId = getCurrentUserId();
        return ApiResponse.success(goalService.getGoalsByUser(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<LearningGoal> getGoal(@PathVariable Long id) {
        LearningGoal goal = goalService.getGoalById(id);
        if (goal == null) {
            return ApiResponse.error(404, "目标不存在");
        }
        return ApiResponse.success(goal);
    }

    @PostMapping
    public ApiResponse<LearningGoal> createGoal(@RequestBody LearningGoal goal) {
        Long userId = getCurrentUserId();
        LearningGoal saved = goalService.createGoal(goal, userId);
        return ApiResponse.success("创建成功", saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<LearningGoal> updateGoal(@PathVariable Long id, @RequestBody LearningGoal goal) {
        goal.setId(id);
        LearningGoal updated = goalService.updateGoal(goal);
        return ApiResponse.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ApiResponse.success("删除成功", null);
    }
}