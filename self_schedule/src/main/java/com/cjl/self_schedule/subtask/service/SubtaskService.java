package com.cjl.self_schedule.subtask.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjl.self_schedule.subtask.entity.Subtask;
import java.util.List;

public interface SubtaskService extends IService<Subtask> {
    Subtask create(Long taskId, String title);
    Subtask update(Long id, String title);
    boolean updateById(Subtask subtask);
    void delete(Long id);
    Subtask complete(Long id);
    List<Subtask> findByTaskId(Long taskId);
    int countByTaskId(Long taskId);
    int countCompletedByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}