package com.example.library.entity;

import lombok.Data;

@Data
public class TrendVO {
    private String date;   // 日期，格式如 "07-05"
    private Long count;    // 该日借阅数量
}