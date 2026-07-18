package com.example.library.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookWithCategoryDTO {
    private Integer id;
    private String bookName;
    private String author;
    private BigDecimal price;
    private Integer stock;
    private String description;   // 新增
    private Integer categoryId;
    private String categoryName;
}