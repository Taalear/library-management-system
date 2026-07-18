package com.example.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.mapper.CategoryMapper;
import com.example.library.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Page<Category> getCategoryPage(Integer pageNum, Integer pageSize) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return categoryMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public void addCategory(Category category) {
        // 检查名称是否重复
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, category.getName());
        if (categoryMapper.selectOne(wrapper) != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类名称已存在");
        }
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Category category) {
        if (category.getId() == null || categoryMapper.selectById(category.getId()) == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类不存在");
        }
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Integer id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类不存在");
        }
        categoryMapper.deleteById(id);
    }
}