package com.project.expenseService.feign;

import com.project.expenseService.model.dto.CategoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "category-service", url = "http://localhost:8083") // url is optional if using Eureka but good for local dev
public interface CategoryClient {

    @GetMapping("/categories/{id}")
    ResponseEntity<CategoryDto> getCategoryById(@PathVariable("id") Long id);
}
