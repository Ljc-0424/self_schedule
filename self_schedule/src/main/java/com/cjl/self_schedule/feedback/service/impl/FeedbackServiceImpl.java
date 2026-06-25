package com.cjl.self_schedule.feedback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjl.self_schedule.feedback.entity.Feedback;
import com.cjl.self_schedule.feedback.mapper.FeedbackMapper;
import com.cjl.self_schedule.feedback.service.FeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {
}
