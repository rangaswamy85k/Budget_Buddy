package com.project.notificationService.controller;

import com.project.notificationService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send-remainder")
    public ResponseEntity<String> triggerReminderJob() {
        notificationService.sendDailyIncomeExpenseRemainder();
        return ResponseEntity.ok("Daily Income/Expense Remainder Job triggered successfully!");
    }

    @PostMapping("/send-summary")
    public ResponseEntity<String> triggerSummaryJob() {
        notificationService.sendDailyExpenseSummary();
        return ResponseEntity.ok("Daily Expense Summary Job triggered successfully!");
    }
}
