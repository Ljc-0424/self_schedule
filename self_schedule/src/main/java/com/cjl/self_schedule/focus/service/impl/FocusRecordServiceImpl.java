package com.cjl.self_schedule.focus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjl.self_schedule.focus.dto.FocusRecordQueryDTO;
import com.cjl.self_schedule.focus.entity.FocusRecord;
import com.cjl.self_schedule.focus.mapper.FocusRecordMapper;
import com.cjl.self_schedule.focus.service.FocusRecordService;
import com.cjl.self_schedule.common.utils.QueryWrapperBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FocusRecordServiceImpl extends ServiceImpl<FocusRecordMapper, FocusRecord> implements FocusRecordService {

    @Override
    public List<FocusRecord> queryFocusRecords(Long userId, FocusRecordQueryDTO queryDTO) {
        QueryWrapper<FocusRecord> queryWrapper = QueryWrapperBuilder.buildFocusRecordQueryWrapper(userId, queryDTO);
        return list(queryWrapper);
    }

    @Override
    public IPage<FocusRecord> queryFocusRecordsPage(Long userId, FocusRecordQueryDTO queryDTO) {
        QueryWrapper<FocusRecord> queryWrapper = QueryWrapperBuilder.buildFocusRecordQueryWrapper(userId, queryDTO);

        int pageNum = queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 20;

        Page<FocusRecord> page = new Page<>(pageNum, pageSize);
        return page(page, queryWrapper);
    }
}