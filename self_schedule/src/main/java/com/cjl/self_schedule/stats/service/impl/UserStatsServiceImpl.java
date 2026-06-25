package com.cjl.self_schedule.stats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cjl.self_schedule.auth.entity.User;
import com.cjl.self_schedule.auth.service.UserService;
import com.cjl.self_schedule.focus.dto.DailyFocusStat;
import com.cjl.self_schedule.focus.mapper.FocusRecordMapper;
import com.cjl.self_schedule.stats.service.UserStatsService;
import com.cjl.self_schedule.stats.vo.BadgeVO;
import com.cjl.self_schedule.stats.vo.UserStatsVO;
import com.cjl.self_schedule.task.entity.Task;
import com.cjl.self_schedule.task.mapper.TaskMapper;
import com.cjl.self_schedule.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserStatsServiceImpl implements UserStatsService {

    @Autowired
    private FocusRecordMapper focusRecordMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private static final int DEFAULT_DAILY_FOCUS_GOAL = 7200;
    private static final int DEFAULT_MIN_EFFECTIVE_DURATION = 1800;

    private static final Map<String, String[]> CATEGORY_KEYWORD_MAP = new LinkedHashMap<>();
    static {
        CATEGORY_KEYWORD_MAP.put("生活规划家", new String[]{"日常", "生活", "作息", "饮食", "睡眠", "家务", "购物", "清洁", "烹饪"});
        CATEGORY_KEYWORD_MAP.put("学习求知者", new String[]{"学习", "备考", "考研", "读书", "课程", "笔记", "复习", "考试", "英语", "编程", "数学"});
        CATEGORY_KEYWORD_MAP.put("职场奋斗者", new String[]{"工作", "办公", "项目", "会议", "报告", "职场", "述职", "汇报", "开发", "需求"});
        CATEGORY_KEYWORD_MAP.put("运动活力派", new String[]{"运动", "健身", "跑步", "锻炼", "瑜伽", "游泳", "散步", "拉伸", "骑行"});
        CATEGORY_KEYWORD_MAP.put("休闲爱好者", new String[]{"娱乐", "放松", "游戏", "电影", "音乐", "旅行", "休闲", "阅读", "绘画"});
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO stats = new UserStatsVO();

        User user = userService.getById(userId);
        int dailyFocusGoal = user != null && user.getDailyFocusGoal() != null ? user.getDailyFocusGoal() : DEFAULT_DAILY_FOCUS_GOAL;
        int minEffectiveDuration = user != null && user.getMinEffectiveDuration() != null ? user.getMinEffectiveDuration() : DEFAULT_MIN_EFFECTIVE_DURATION;

        computeFocusStats(stats, userId, dailyFocusGoal);
        computeTaskStats(stats, userId);
        computeCheckInStats(stats, userId, minEffectiveDuration);
        computeBadges(stats, userId);

        stats.setDailyFocusGoalSeconds(dailyFocusGoal);
        stats.setMinEffectiveDurationSeconds(minEffectiveDuration);

        return stats;
    }

    private void computeFocusStats(UserStatsVO stats, Long userId, int dailyFocusGoal) {
        Long totalFocus = focusRecordMapper.getTotalFocusDuration(userId);
        stats.setTotalFocusSeconds(totalFocus != null ? totalFocus : 0L);

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        Long todayFocus = focusRecordMapper.getFocusDurationInRange(userId, todayStart, tomorrowStart);
        stats.setTodayFocusSeconds(todayFocus != null ? todayFocus : 0L);

        Long monthlyFocus = focusRecordMapper.getFocusDurationInRange(userId, monthStart, tomorrowStart);
        stats.setMonthlyFocusSeconds(monthlyFocus != null ? monthlyFocus : 0L);

        if (dailyFocusGoal > 0) {
            double rate = (double) stats.getTodayFocusSeconds() / dailyFocusGoal;
            stats.setTodayGoalRate(Math.round(rate * 1000) / 1000.0);
        } else {
            stats.setTodayGoalRate(0.0);
        }

        Long maxDaily = focusRecordMapper.getMaxDailyFocusDuration(userId);
        stats.setMaxDailyFocusSeconds(maxDaily != null ? maxDaily : 0L);
    }

    private void computeTaskStats(UserStatsVO stats, Long userId) {
        Integer totalCompleted = taskMapper.countCompletedTasks(userId);
        stats.setTotalCompletedTasks(totalCompleted != null ? totalCompleted : 0);

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        Integer todayCompleted = taskMapper.countCompletedTasksInRange(userId, todayStart, tomorrowStart);
        stats.setTodayCompletedTasks(todayCompleted != null ? todayCompleted : 0);

        Integer weekCompleted = taskMapper.countCompletedTasksInRange(userId, weekStart, tomorrowStart);
        stats.setWeekCompletedTasks(weekCompleted != null ? weekCompleted : 0);

        Integer monthCompleted = taskMapper.countCompletedTasksInRange(userId, monthStart, tomorrowStart);
        stats.setMonthCompletedTasks(monthCompleted != null ? monthCompleted : 0);

        int todayTotal = todayCompleted != null ? todayCompleted : 0;
        int todayActive = countActiveTasks(userId, todayStart, tomorrowStart);
        stats.setTodayTaskRate(calcRate(todayTotal, todayTotal + todayActive));

        int weekTotal = weekCompleted != null ? weekCompleted : 0;
        int weekActive = countActiveTasks(userId, weekStart, tomorrowStart);
        stats.setWeekTaskRate(calcRate(weekTotal, weekTotal + weekActive));

        int monthTotal = monthCompleted != null ? monthCompleted : 0;
        int monthActive = countActiveTasks(userId, monthStart, tomorrowStart);
        stats.setMonthTaskRate(calcRate(monthTotal, monthTotal + monthActive));
    }

    private int countActiveTasks(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<Task> qw = new QueryWrapper<>();
        qw.eq("user_id", userId)
          .in("status", 0, 1)
          .ge("created_time", startTime)
          .lt("created_time", endTime);
        return (int) taskService.count(qw);
    }

    private double calcRate(int completed, int total) {
        if (total <= 0) return 0.0;
        return (double) completed / total;
    }

    private void computeCheckInStats(UserStatsVO stats, Long userId, int minEffectiveDuration) {
        List<DailyFocusStat> dailyStats = focusRecordMapper.getDailyFocusTotals(userId);

        List<LocalDate> checkInDates = dailyStats.stream()
                .filter(d -> d.getTotalDuration() != null && d.getTotalDuration() >= minEffectiveDuration)
                .map(DailyFocusStat::getFocusDate)
                .collect(Collectors.toList());

        stats.setTotalCheckInDays(checkInDates.size());

        int currentStreak = computeCurrentStreak(checkInDates);
        stats.setCurrentStreak(currentStreak);

        int maxStreak = computeMaxStreak(checkInDates);
        stats.setMaxStreak(maxStreak);
    }

    private int computeCurrentStreak(List<LocalDate> checkInDates) {
        if (checkInDates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        if (!checkInDates.contains(today)) {
            checkDate = today.minusDays(1);
        }

        while (checkInDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private int computeMaxStreak(List<LocalDate> checkInDates) {
        if (checkInDates.isEmpty()) return 0;

        List<LocalDate> sorted = checkInDates.stream().sorted().collect(Collectors.toList());
        int maxStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return maxStreak;
    }

    private void computeBadges(UserStatsVO stats, Long userId) {
        List<BadgeVO> badges = new ArrayList<>();

        long totalHours = stats.getTotalFocusSeconds() / 3600;
        badges.add(new BadgeVO("persistence_beginner", "坚持萌新", "🌱",
                "累计专注时长 ≥10 小时", "PERSISTENCE",
                totalHours >= 10, formatProgress(totalHours, 10, "小时")));

        badges.add(new BadgeVO("self_discipline", "自律达人", "🔥",
                "连续打卡 7 天", "PERSISTENCE",
                stats.getCurrentStreak() >= 7, formatProgress(stats.getCurrentStreak(), 7, "天")));

        badges.add(new BadgeVO("hundred_days", "百日坚守", "💯",
                "累计打卡 100 天", "PERSISTENCE",
                stats.getTotalCheckInDays() >= 100, formatProgress(stats.getTotalCheckInDays(), 100, "天")));

        badges.add(new BadgeVO("time_master", "时间大师", "⏳",
                "累计专注时长 ≥500 小时", "PERSISTENCE",
                totalHours >= 500, formatProgress(totalHours, 500, "小时")));

        computeSpecialtyBadges(badges, userId);

        badges.add(new BadgeVO("efficient_executor", "高效执行者", "⚡",
                "累计完成任务 ≥100 个", "EFFICIENCY",
                stats.getTotalCompletedTasks() >= 100, formatProgress(stats.getTotalCompletedTasks(), 100, "个")));

        long maxDailyHours = stats.getMaxDailyFocusSeconds() / 3600;
        badges.add(new BadgeVO("focus_pioneer", "专注先锋", "🚀",
                "单日最高专注 ≥4 小时", "EFFICIENCY",
                stats.getMaxDailyFocusSeconds() >= 14400, formatProgress(maxDailyHours, 4, "小时")));

        Integer distinctCategories = taskMapper.countDistinctCompletedCategories(userId);
        int catCount = distinctCategories != null ? distinctCategories : 0;
        badges.add(new BadgeVO("versatile", "多面能手", "🎯",
                "≥3种不同分类均有完成", "EFFICIENCY",
                catCount >= 3, formatProgress(catCount, 3, "种")));

        stats.setBadges(badges);
    }

    private void computeSpecialtyBadges(List<BadgeVO> badges, Long userId) {
        QueryWrapper<Task> qw = new QueryWrapper<>();
        qw.eq("user_id", userId)
          .eq("status", 2)
          .isNotNull("category")
          .ne("category", "");
        List<Task> completedTasks = taskService.list(qw);

        Map<String, Long> categoryCounts = completedTasks.stream()
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));

        Map<String, Long> badgeScores = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : CATEGORY_KEYWORD_MAP.entrySet()) {
            String badgeName = entry.getKey();
            String[] keywords = entry.getValue();
            long score = 0;
            for (Map.Entry<String, Long> catEntry : categoryCounts.entrySet()) {
                String cat = catEntry.getKey().toLowerCase();
                for (String kw : keywords) {
                    if (cat.contains(kw.toLowerCase())) {
                        score += catEntry.getValue();
                        break;
                    }
                }
            }
            badgeScores.put(badgeName, score);
        }

        long totalCompleted = categoryCounts.values().stream().mapToLong(Long::longValue).sum();
        String[] badgeIcons = {"🏠", "📚", "💼", "🏃", "🎮"};
        String[] badgeIds = {"life_planner", "knowledge_seeker", "career_warrior", "sports_activist", "leisure_lover"};
        int idx = 0;

        for (Map.Entry<String, Long> entry : badgeScores.entrySet()) {
            String badgeName = entry.getKey();
            long score = entry.getValue();
            double rate = totalCompleted > 0 ? (double) score / totalCompleted : 0;
            String[] keywords = CATEGORY_KEYWORD_MAP.get(badgeName);

            badges.add(new BadgeVO(badgeIds[idx], badgeName, badgeIcons[idx],
                    keywords != null ? String.join("/", keywords) + "类任务占比最高" : "",
                    "SPECIALTY",
                    score > 0 && rate >= 0.3,
                    String.format("%.0f%% (%d个)", rate * 100, score)));
            idx++;
        }
    }

    private String formatProgress(long current, long target, String unit) {
        if (current >= target) {
            return "✓ 已达成";
        }
        return current + "/" + target + unit;
    }
}
