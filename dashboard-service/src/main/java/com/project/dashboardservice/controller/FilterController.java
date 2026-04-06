package com.project.dashboardservice.controller;

import com.project.dashboardservice.dto.FilterDTO;
import com.project.dashboardservice.service.FilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final FilterService filterService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(
            @RequestBody FilterDTO filter,
            @RequestHeader(value = "X-User-Id") String userIdStr) {
        try {
            Object result = filterService.filterTransactions(filter, userIdStr);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
