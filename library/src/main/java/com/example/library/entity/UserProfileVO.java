package com.example.library.entity;

import lombok.Data;

@Data
public class UserProfileVO {
    private Integer id;
    private String username;
    private String nickname;
    private String role;
    private Long borrowTotal;      // 总借阅次数
    private Long borrowActive;     // 当前借出中数量
}