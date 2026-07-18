package com.example.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.entity.Favorite;
import com.example.library.entity.FavoriteBookDTO;
import com.example.library.mapper.FavoriteMapper;
import com.example.library.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public boolean toggleFavorite(Integer userId, Integer bookId) {
        if (isFavorited(userId, bookId)) {
            // 已收藏 → 取消收藏
            LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Favorite::getUserId, userId)
                    .eq(Favorite::getBookId, bookId);
            favoriteMapper.delete(wrapper);
            return false; // 取消后未收藏
        } else {
            // 未收藏 → 添加收藏
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setBookId(bookId);
            favorite.setCreatedAt(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            return true; // 收藏后已收藏
        }
    }

    @Override
    public boolean isFavorited(Integer userId, Integer bookId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getBookId, bookId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<FavoriteBookDTO> getFavoritesByUserId(Integer userId) {
        return favoriteMapper.selectFavoritesByUserId(userId);
    }
}