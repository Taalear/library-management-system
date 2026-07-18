package com.example.library.entity;

import lombok.Data;

@Data
public class StatOverviewVO {
    private Long bookCount;      // 图书总数
    private Long userCount;      // 用户总数
    private Long borrowCount;    // 总借阅次数
    private Long activeBorrowCount; // 当前借出中数量
}