package com.project.categoryService.service;

import com.project.categoryService.model.dto.CategoryDto;
import com.project.categoryService.model.entity.Category;
import com.project.categoryService.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public Category createCategory(CategoryDto dto) {
        Category category = Category.builder()
                .name(dto.getName())
                .type(dto.getType())
                .icon(dto.getIcon())
                .userId(dto.getUserId())
                .build();
        return repository.save(category);
    }

    public List<Category> getCategories(Long userId) {
        // Return global categories + user specific categories
        List<Category> global = repository.findByUserIdIsNull();
        if(userId != null) {
            List<Category> userSpecific = repository.findByUserId(userId);
            global.addAll(userSpecific);
        }
        return global;
    }
    
    public Category getCategoryById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
