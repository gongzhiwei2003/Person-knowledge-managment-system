package com.learn.knowledgesystem.controller;

import com.learn.knowledgesystem.common.ApiResponse;
import com.learn.knowledgesystem.dto.NodeCompleteDTO;
import com.learn.knowledgesystem.entity.Roadmap;
import com.learn.knowledgesystem.service.RoadmapService;
import com.learn.knowledgesystem.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {

    @Autowired
    private RoadmapService roadmapService;

    @GetMapping
    public ApiResponse<List<Roadmap>> getUserRoadmaps() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(roadmapService.getUserRoadmaps(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Roadmap> getRoadmap(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        Roadmap roadmap = roadmapService.getRoadmap(id, userId);
        return ApiResponse.success(roadmap);
    }

    @PostMapping("/{id}/share")
    public ApiResponse<String> generateRoadmapShareId(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        String shareId = roadmapService.generateShareId(id, userId);
        return ApiResponse.success(shareId);
    }

    @PostMapping("/{id}/nodes/complete")
    public ApiResponse<?> updateNodeCompletion(
            @PathVariable Long id,
            @RequestBody NodeCompleteDTO dto) {
        // 1. 获取当前登录用户的ID
        Long userId = SecurityUtil.getCurrentUserId();
        // 2. 调用 service 方法更新节点状态
        roadmapService.updateNodeCompletion(userId, id, dto.getNodeId(), dto.isCompleted());
        // 3. 返回成功响应
        return ApiResponse.success("更新成功", null);
    }

    @PostMapping
    public ApiResponse<Roadmap> createRoadmap(@RequestBody Roadmap roadmap) {
        Long userId = SecurityUtil.getCurrentUserId();
        Roadmap saved = roadmapService.createRoadmap(roadmap, userId);
        return ApiResponse.success("创建成功", saved);
    }

    @PutMapping("/{id}/graph")
    public ApiResponse<Roadmap> updateGraph(@PathVariable Long id, @RequestBody Map<String, Object> graphData) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 将 graphData 转换为 JSON 字符串存储
        String json = new ObjectMapper().writeValueAsString(graphData);
        Roadmap updated = roadmapService.updateRoadmapGraph(id, userId, json);
        return ApiResponse.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRoadmap(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        roadmapService.deleteRoadmap(id, userId);
        return ApiResponse.success("删除成功", null);
    }

}
