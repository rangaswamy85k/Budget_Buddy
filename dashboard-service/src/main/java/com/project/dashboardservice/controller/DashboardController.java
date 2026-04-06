package com.project.dashboardservice.controller;

import com.project.dashboardservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestHeader(value = "X-User-Id") String userIdStr) {
        Map<String, Object> dashboardData = dashboardService.getDashboardData(userIdStr);
        return ResponseEntity.ok(dashboardData);
    }
}
