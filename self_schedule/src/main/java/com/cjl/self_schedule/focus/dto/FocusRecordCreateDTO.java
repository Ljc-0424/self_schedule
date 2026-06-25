package com.cjl.self_schedule.focus.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FocusRecordCreateDTO {
    private Long taskId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer duration;
    private Integer interruptions;
    private Integer focusScore;
    private String notes;
    private Integer status;
}