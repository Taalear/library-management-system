package com.example.library.service;

import com.example.library.entity.FavoriteBookDTO;

import java.util.List;

public interface FavoriteService {
    // 切换收藏状态（如果已收藏则取消，未收藏则添加）
    boolean toggleFavorite(Integer userId, Integer bookId);

    // 检查是否已收藏
    boolean isFavorited(Integer userId, Integer bookId);

    // 获取用户的所有收藏图书
    List<FavoriteBookDTO> getFavoritesByUserId(Integer userId);
}