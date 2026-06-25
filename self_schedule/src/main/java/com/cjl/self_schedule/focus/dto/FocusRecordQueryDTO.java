package com.cjl.self_schedule.focus.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FocusRecordQueryDTO {
    private Long taskId;
    private LocalDateTime startTimeAfter;
    private LocalDateTime endTimeBefore;
    
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    
    public Integer getPageNum() {
        return pageNum != null && pageNum > 0 ? pageNum : 1;
    }
    
    public Integer getPageSize() {
        if (pageSize == null || pageSize <= 0) return 20;
        return Math.min(pageSize, 100);
    }
}