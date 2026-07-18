package com.example.library.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteBookDTO {
    private Integer id;           // 收藏记录ID
    private Integer bookId;       // 图书ID
    private String bookName;      // 书名
    private String author;        // 作者
    private BigDecimal price;     // 价格
    private Integer stock;        // 库存
    private String categoryName;  // 分类名称
    private LocalDateTime favoritedAt; // 收藏时间
}