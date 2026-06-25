package com.cjl.self_schedule.stats.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserStatsVO {
    private Long totalFocusSeconds;
    private Long monthlyFocusSeconds;
    private Long todayFocusSeconds;
    private Double todayGoalRate;
    private Long maxDailyFocusSeconds;
    private Integer dailyFocusGoalSeconds;

    private Integer totalCompletedTasks;
    private Integer todayCompletedTasks;
    private Integer weekCompletedTasks;
    private Integer monthCompletedTasks;
    private Double todayTaskRate;
    private Double weekTaskRate;
    private Double monthTaskRate;

    private Integer currentStreak;
    private Integer maxStreak;
    private Integer totalCheckInDays;
    private Integer minEffectiveDurationSeconds;

    private List<BadgeVO> badges;
}
