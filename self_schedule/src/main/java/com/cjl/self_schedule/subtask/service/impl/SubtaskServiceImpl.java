package com.cjl.self_schedule.subtask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjl.self_schedule.subtask.entity.Subtask;
import com.cjl.self_schedule.subtask.mapper.SubtaskMapper;
import com.cjl.self_schedule.subtask.service.SubtaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubtaskServiceImpl extends ServiceImpl<SubtaskMapper, Subtask> implements SubtaskService {

    @Override
    @Transactional
    public Subtask create(Long taskId, String title) {
        int count = baseMapper.countByTaskId(taskId);
        
        Subtask subtask = new Subtask();
        subtask.setTaskId(taskId);
        subtask.setTitle(title);
        subtask.setStatus(0);
        subtask.setSortOrder(count);
        subtask.setCreatedTime(LocalDateTime.now());
        
        baseMapper.insert(subtask);
        return subtask;
    }

    @Override
    @Transactional
    public Subtask update(Long id, String title) {
        Subtask subtask = baseMapper.selectById(id);
        if (subtask != null) {
            subtask.setTitle(title);
            baseMapper.updateById(subtask);
        }
        return subtask;
    }

    @Override
    @Transactional
    public boolean updateById(Subtask subtask) {
        return baseMapper.updateById(subtask) > 0;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Subtask complete(Long id) {
        Subtask subtask = baseMapper.selectById(id);
        if (subtask != null) {
            if (subtask.getStatus() == 2) {
                subtask.setStatus(0);
                subtask.setCompletedTime(null);
            } else {
                subtask.setStatus(2);
                subtask.setCompletedTime(LocalDateTime.now());
            }
            baseMapper.updateById(subtask);
        }
        return subtask;
    }

    @Override
    public List<Subtask> findByTaskId(Long taskId) {
        return baseMapper.findByTaskId(taskId);
    }

    @Override
    public int countByTaskId(Long taskId) {
        return baseMapper.countByTaskId(taskId);
    }

    @Override
    public int countCompletedByTaskId(Long taskId) {
        return baseMapper.countCompletedByTaskId(taskId);
    }

    @Override
    @Transactional
    public void deleteByTaskId(Long taskId) {
        baseMapper.delete(new QueryWrapper<Subtask>().eq("task_id", taskId));
    }
}