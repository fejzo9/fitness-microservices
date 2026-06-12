package com.app.fitness.controller;

import com.app.fitness.model.NotificationLog;
import com.app.fitness.service.WorkoutNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationLogController {

    private final WorkoutNotificationService workoutNotificationService;

    @GetMapping("/latest/{userId}")
    public List<NotificationLog> getLatestNotifications(@PathVariable Long userId) {
        return workoutNotificationService.getLatestNotifications(userId);
    }
}
