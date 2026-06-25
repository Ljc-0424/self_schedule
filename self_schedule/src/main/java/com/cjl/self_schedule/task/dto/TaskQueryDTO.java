package com.cjl.self_schedule.task.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskQueryDTO {
    private String category;
    private List<Integer> statusList;
    private List<Integer> priorityList;
    private LocalDateTime deadlineBefore;
    private String keyword;
    
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