package com.cjl.self_schedule.focus.service;

import com.cjl.self_schedule.focus.entity.FocusRecord;
import com.cjl.self_schedule.focus.dto.FocusRecordQueryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FocusRecordService extends IService<FocusRecord> {
    List<FocusRecord> queryFocusRecords(Long userId, FocusRecordQueryDTO queryDTO);
    IPage<FocusRecord> queryFocusRecordsPage(Long userId, FocusRecordQueryDTO queryDTO);
}