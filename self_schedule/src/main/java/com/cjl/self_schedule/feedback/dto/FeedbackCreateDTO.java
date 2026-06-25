package com.cjl.self_schedule.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackCreateDTO {
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String category;

    @NotBlank(message = "反馈标题不能为空")
    @Size(max = 200, message = "反馈标题不能超过200个字符")
    private String title;

    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 5000, message = "反馈内容不能超过5000个字符")
    private String content;

    @Size(max = 200, message = "联系方式不能超过200个字符")
    private String contact;
}
