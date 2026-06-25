package com.cjl.self_schedule.feedback.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String category;
    private String title;
    private String content;
    private String contact;
    private Integer status;
    private String adminReply;
    private Integer userRead;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
