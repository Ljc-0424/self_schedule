package com.cjl.self_schedule.focus.vo;

import com.cjl.self_schedule.task.vo.TaskVO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FocusRecordVO {
    private Long id;
    private Long userId;
    private Long taskId;
    private String taskTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer interruptions;
    private Integer focusScore;
    private String notes;
    private Integer status;
    private LocalDateTime createdTime;
    
    private TaskVO task;
}