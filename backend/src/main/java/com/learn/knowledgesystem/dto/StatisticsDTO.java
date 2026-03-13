package com.learn.knowledgesystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class StatisticsDTO {
    private List<NoteTrendDTO> noteTrend;
    private List<TagCountDTO> tagCounts;
    private GoalProgressDTO goalProgress;

    @Data
    public static class NoteTrendDTO {
        private String month;
        private long count;
    }

    @Data
    public static class TagCountDTO {
        private String name;
        private long count;
    }

    @Data
    public static class GoalProgressDTO {
        private long totalGoals;
        private long completedGoals;
        private double completionRate;
    }
}