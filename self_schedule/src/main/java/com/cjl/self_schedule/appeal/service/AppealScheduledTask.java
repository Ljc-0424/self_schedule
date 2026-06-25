package com.cjl.self_schedule.appeal.service;

import com.cjl.self_schedule.appeal.service.impl.AppealServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AppealScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(AppealScheduledTask.class);

    @Autowired
    private AppealService appealService;

    @Scheduled(fixedRate = 300000)
    public void unbanExpiredUsers() {
        try {
            appealService.unbanExpiredUsers();
        } catch (Exception e) {
            log.error("自动解封到期用户失败: {}", e.getMessage());
        }
    }
}
