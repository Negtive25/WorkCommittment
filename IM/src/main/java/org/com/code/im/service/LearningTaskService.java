package org.com.code.im.service;

import org.com.code.im.pojo.LearningTask;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface LearningTaskService {

    Map<String, Object> createTask(LearningTask task);

    List<Map<String, Object>> getAllTasks(Long userId);

    List<Map<String, Object>> getTasksByPlanId(Long planId, Long userId);

    Map<String, Object> getTaskDetails(Long taskId, Long userId);

    boolean updateTask(LearningTask task);

    boolean deleteTask(Long taskId, Long userId);

    boolean markTaskAsComplete(Long taskId, Long userId);

    // Methods for scheduler
    void resetDailyTaskCompletionStatus();

    void triggerReminders();
} 