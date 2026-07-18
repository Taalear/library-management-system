package com.example.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.entity.User;
import com.example.library.entity.UserProfileVO;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public boolean save(User user) {
        return userMapper.insert(user) > 0;
    }

    @Override
    public UserProfileVO getUserProfile(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        // 统计借阅总数
        LambdaQueryWrapper<com.example.library.entity.BorrowRecord> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(com.example.library.entity.BorrowRecord::getUserId, userId);
        vo.setBorrowTotal(borrowRecordMapper.selectCount(totalWrapper));
        // 统计借出中数量
        LambdaQueryWrapper<com.example.library.entity.BorrowRecord> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(com.example.library.entity.BorrowRecord::getUserId, userId)
                .eq(com.example.library.entity.BorrowRecord::getStatus, 0);
        vo.setBorrowActive(borrowRecordMapper.selectCount(activeWrapper));
        return vo;
    }

    @Override
    public void updateNickname(Integer userId, String nickname) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setNickname(nickname);
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "原密码错误");
        }
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public boolean updateById(User user) {
        return userMapper.updateById(user) > 0;
    }
}