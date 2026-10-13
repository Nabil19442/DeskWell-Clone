package com.deskwell.web.controller;

import com.deskwell.web.model.Application;
import com.deskwell.web.model.Category;
import com.deskwell.web.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String color = (String) body.get("color");
        Double weight = body.get("productivityWeight") != null ? Double.parseDouble(body.get("productivityWeight").toString()) : null;
        return ResponseEntity.ok(categoryService.updateCategory(id, name, color, weight));
    }

    @GetMapping("/apps")
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(categoryService.getAllApplications());
    }

    @PostMapping("/assign")
    public ResponseEntity<Application> assignCategory(@RequestBody Map<String, Long> payload) {
        Long appId = payload.get("applicationId");
        Long categoryId = payload.get("categoryId");
        return ResponseEntity.ok(categoryService.assignCategory(appId, categoryId));
    }
}
