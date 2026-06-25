package com.cjl.self_schedule.focus.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyFocusStat {
    private LocalDate focusDate;
    private Long totalDuration;
}
