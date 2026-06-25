package com.cjl.self_schedule.appeal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjl.self_schedule.appeal.entity.Appeal;
import com.cjl.self_schedule.appeal.vo.BanInfoVO;

public interface AppealService extends IService<Appeal> {

    BanInfoVO getBanInfo(String username);

    void submitAppeal(String username, String content);

    void auditAppeal(Long appealId, Integer status, String auditNote, Long adminId);

    void unbanExpiredUsers();
}
