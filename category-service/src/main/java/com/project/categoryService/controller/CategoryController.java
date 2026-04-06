package com.project.categoryService.controller;

import com.project.categoryService.model.dto.CategoryDto;
import com.project.categoryService.model.entity.Category;
import com.project.categoryService.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CategoryDto dto, @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        // If X-User-Id is present, assign it.
        if (userIdStr != null) {
            dto.setUserId(Long.parseLong(userIdStr));
        }
        return ResponseEntity.ok(service.createCategory(dto));
    }

    @GetMapping
    public ResponseEntity<List<Category>> getCategories(@RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
        return ResponseEntity.ok(service.getCategories(userId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCategoryById(id));
    }
}
