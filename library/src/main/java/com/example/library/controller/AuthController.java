package com.example.library.controller;

import com.example.library.entity.Result;
import com.example.library.entity.User;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.service.UserService;
import com.example.library.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody User user) {
        if (userService.findByUsername(user.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        user.setPassword(encoder.encode(user.getPassword()));
        userService.save(user);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody User loginUser) {
        try {
            User dbUser = userService.findByUsername(loginUser.getUsername());
            if (dbUser == null || !encoder.matches(loginUser.getPassword(), dbUser.getPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_ERROR);
            }
            String token = jwtUtil.generateToken(dbUser.getUsername(), dbUser.getRole());
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            data.put("role", dbUser.getRole());
            data.put("username", dbUser.getUsername());
            // 🔥 新增：返回头像
            data.put("avatar", dbUser.getAvatar() != null ? dbUser.getAvatar() : "");
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage());
        }
    }
}