package com.example.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.Category;
import com.example.library.entity.Result;
import com.example.library.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理", description = "图书分类接口")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ===== 公开：查询所有分类（无需登录） =====
    @Operation(summary = "查询所有分类", description = "公开接口，用于前台下拉框")
    @GetMapping("/all")
    public Result<List<Category>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    // ===== 以下接口需要登录（由 SecurityConfig 控制） =====
    @Operation(summary = "分页查询分类", description = "管理员专用")
    @GetMapping
    public Result<Page<Category>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(categoryService.getCategoryPage(pageNum, pageSize));
    }

    @Operation(summary = "新增分类", description = "管理员专用")
    @PostMapping
    public Result<String> addCategory(@Valid @RequestBody Category category) {
        categoryService.addCategory(category);
        return Result.success("新增分类成功");
    }

    @Operation(summary = "修改分类", description = "管理员专用")
    @PutMapping
    public Result<String> updateCategory(@Valid @RequestBody Category category) {
        categoryService.updateCategory(category);
        return Result.success("修改分类成功");
    }

    @Operation(summary = "删除分类", description = "管理员专用")
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return Result.success("删除分类成功");
    }
}