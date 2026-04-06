package in.project.category.controller;

import in.project.category.dto.CategoryDTO;
import in.project.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    //Save a new category
    @PostMapping
    public ResponseEntity<CategoryDTO> saveCategory(@RequestBody CategoryDTO categoryDTO, @RequestHeader("X-USER-ID") Long userId, @RequestHeader("X-USERNAME") String username){
        CategoryDTO savedCategory = categoryService.saveCategory(categoryDTO, userId);
        System.out.println(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    //Get all categories for the current user
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories(@RequestHeader("X-User-Id") Long userId){
      List<CategoryDTO> categories=  categoryService.getCategoriesForCurrentUser(userId);
      return ResponseEntity.ok(categories);
    }

    //Get categories by type for the current user
    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByTypeFprCurrentUser(@PathVariable String type, @RequestHeader("X-User-Id") Long userId){
        List<CategoryDTO> list =  categoryService.getCategoriesByTypeForCurrentUser(type, userId);
        return ResponseEntity.ok(list);
    }

    //Update an existing category and ensure it belongs to the current user
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO, @RequestHeader("X-User-Id") Long userId){
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, categoryDTO, userId);
        return ResponseEntity.ok(updatedCategory);
    }
}
