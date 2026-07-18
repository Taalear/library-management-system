package com.example.library.service;

import com.example.library.entity.User;
import com.example.library.entity.UserProfileVO;

public interface UserService {
    User findByUsername(String username);
    boolean save(User user);
    UserProfileVO getUserProfile(Integer userId);
    void updateNickname(Integer userId, String nickname);
    void updatePassword(Integer userId, String oldPassword, String newPassword);
    boolean updateById(User user);  // ← 新增
}