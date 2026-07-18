package com.example.library.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Integer id;
    private Integer bookId;
    private Integer userId;
    private String username;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}