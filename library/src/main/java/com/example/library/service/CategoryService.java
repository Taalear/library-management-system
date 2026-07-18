package com.example.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.Category;

import java.util.List;

public interface CategoryService {
    // 分页查询分类
    Page<Category> getCategoryPage(Integer pageNum, Integer pageSize);
    // 查询所有分类（用于下拉框）
    List<Category> getAllCategories();
    // 新增分类
    void addCategory(Category category);
    // 修改分类
    void updateCategory(Category category);
    // 删除分类
    void deleteCategory(Integer id);
}