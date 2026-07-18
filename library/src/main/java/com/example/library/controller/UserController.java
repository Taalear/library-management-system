package com.example.library.controller;

import com.example.library.entity.Result;
import com.example.library.entity.User;
import com.example.library.entity.UserProfileVO;
import com.example.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        User user = getCurrentUser();
        return Result.success(userService.getUserProfile(user.getId()));
    }

    @PutMapping("/nickname")
    public Result<String> updateNickname(@RequestBody Map<String, String> params) {
        User user = getCurrentUser();
        String nickname = params.get("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            return Result.error(1001, "昵称不能为空");
        }
        userService.updateNickname(user.getId(), nickname.trim());
        return Result.success("昵称修改成功");
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody Map<String, String> params) {
        User user = getCurrentUser();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        String confirmPassword = params.get("confirmPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.error(1001, "请输入原密码");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error(1001, "请输入新密码");
        }
        if (newPassword.length() < 6) {
            return Result.error(1001, "新密码长度不能少于6位");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.error(1001, "两次输入密码不一致");
        }

        userService.updatePassword(user.getId(), oldPassword.trim(), newPassword.trim());
        return Result.success("密码修改成功，请重新登录");
    }

    // ===== 头像上传（修复版） =====
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            User user = getCurrentUser();

            // 1. 获取项目根目录绝对路径
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "avatars" + File.separator;

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return Result.error(1001, "无法创建头像存储目录");
                }
            }

            // 2. 生成唯一文件名
            String fileName = user.getId() + "_" + System.currentTimeMillis() + ".jpg";
            String filePath = uploadDir + fileName;

            // 3. 保存文件
            file.transferTo(new File(filePath));

            // 4. 更新数据库（存储相对路径，用于前端访问）
            // 假设前端可通过 /uploads/avatars/xxx.jpg 访问，需配置静态资源映射
            String relativePath = "/uploads/avatars/" + fileName;
            user.setAvatar(relativePath);
            userService.updateById(user);

            return Result.success(relativePath);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(1000, "文件保存失败：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(1000, "系统繁忙，请稍后重试");
        }
    }

    // ===== 工具方法 =====
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在，请重新登录");
        }
        return user;
    }
}