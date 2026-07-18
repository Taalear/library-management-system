package com.example.library.controller;

import com.example.library.entity.FavoriteBookDTO;
import com.example.library.entity.Result;
import com.example.library.entity.User;
import com.example.library.service.FavoriteService;
import com.example.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    /**
     * 切换收藏状态（收藏/取消收藏）
     */
    @PostMapping("/toggle/{bookId}")
    public Result<Boolean> toggleFavorite(@PathVariable Integer bookId) {
        User user = getCurrentUser();
        boolean result = favoriteService.toggleFavorite(user.getId(), bookId);
        return Result.success(result);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{bookId}")
    public Result<Boolean> checkFavorited(@PathVariable Integer bookId) {
        User user = getCurrentUser();
        boolean result = favoriteService.isFavorited(user.getId(), bookId);
        return Result.success(result);
    }

    /**
     * 获取我的所有收藏
     */
    @GetMapping("/my")
    public Result<List<FavoriteBookDTO>> getMyFavorites() {
        User user = getCurrentUser();
        List<FavoriteBookDTO> list = favoriteService.getFavoritesByUserId(user.getId());
        return Result.success(list);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在，请重新登录");
        }
        return user;
    }
}