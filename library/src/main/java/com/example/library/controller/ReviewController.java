package com.example.library.controller;

import com.example.library.entity.Result;
import com.example.library.entity.Review;
import com.example.library.entity.ReviewDTO;
import com.example.library.entity.User;
import com.example.library.mapper.ReviewMapper;
import com.example.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserService userService;

    /**
     * 获取图书的所有评论
     */
    @GetMapping("/book/{bookId}")
    public Result<Map<String, Object>> getReviewsByBook(@PathVariable Integer bookId) {
        List<ReviewDTO> reviews = reviewMapper.selectReviewsByBookId(bookId);
        Double avgRating = reviewMapper.selectAvgRatingByBookId(bookId);
        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviews);
        result.put("avgRating", avgRating != null ? String.format("%.1f", avgRating) : "0.0");
        result.put("count", reviews.size());
        return Result.success(result);
    }

    /**
     * 用户对图书评分/评论（需登录）
     */
    @PostMapping
    public Result<String> addReview(@RequestBody Review review) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error(2001, "用户不存在，请重新登录");
        }
        // 检查用户是否已借阅过该书（防止恶意刷评）
        // 简化处理：只校验评分范围
        if (review.getRating() < 1 || review.getRating() > 5) {
            return Result.error(1001, "评分必须在1-5之间");
        }
        review.setUserId(user.getId());
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        return Result.success("评价成功");
    }
}