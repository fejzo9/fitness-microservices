package com.app.fitness.controller;

import com.app.fitness.service.WorkoutNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/notifications/test")
@RequiredArgsConstructor
public class TestNotificationController {

    private final WorkoutNotificationService workoutNotificationService;

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerNotifications(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        workoutNotificationService.processNotificationsForDate(targetDate);
        return ResponseEntity.ok("Notification processing triggered for date: " + targetDate);
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetFlags() {
        workoutNotificationService.resetNotificationFlags();
        return ResponseEntity.ok("All notification flags reset to false");
    }
}
