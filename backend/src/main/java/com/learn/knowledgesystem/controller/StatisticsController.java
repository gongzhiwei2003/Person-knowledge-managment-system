package com.learn.knowledgesystem.controller;

import com.learn.knowledgesystem.common.ApiResponse;
import com.learn.knowledgesystem.dto.StatisticsDTO;
import com.learn.knowledgesystem.service.StatisticsService;
import com.learn.knowledgesystem.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public ApiResponse<StatisticsDTO> getUserStatistics() {
        Long userId = SecurityUtil.getCurrentUserId();
        StatisticsDTO stats = statisticsService.getUserStatistics(userId);
        return ApiResponse.success(stats);
    }
}