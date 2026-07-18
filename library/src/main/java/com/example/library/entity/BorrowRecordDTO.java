package com.example.library.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowRecordDTO {
    private Integer id;           // 借阅记录ID
    private Integer userId;       // 用户ID
    private String username;      // 用户名（冗余展示）
    private Integer bookId;       // 图书ID
    private String bookName;      // 书名
    private String author;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")// 作者
    private LocalDateTime borrowTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")// 借书时间
    private LocalDateTime returnTime;   // 还书时间

    private Integer status;       // 0-借出中，1-已归还
    private String statusText;    // 状态文本（前端展示用）
}