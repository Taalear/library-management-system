package com.example.library;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("========== 加密结果 ==========");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密密文: " + encodedPassword);
        System.out.println("===============================");
        System.out.println("请复制上面的密文，更新到数据库 user 表的 password 字段");
    }
}