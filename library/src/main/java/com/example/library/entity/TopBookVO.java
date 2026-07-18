package com.example.library.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopBookVO {
    private Integer id;
    private String bookName;
    private String author;
    private BigDecimal price;
    private Integer stock;
    private String categoryName;
    private String description;
    private Long borrowCount;
}