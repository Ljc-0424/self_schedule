package com.cjl.self_schedule.stats.service;

import com.cjl.self_schedule.stats.vo.UserStatsVO;

public interface UserStatsService {
    UserStatsVO getUserStats(Long userId);
}
