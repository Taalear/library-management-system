package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.Review;
import com.example.library.entity.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT r.*, u.username " +
            "FROM review r " +
            "LEFT JOIN user u ON r.user_id = u.id " +
            "WHERE r.book_id = #{bookId} " +
            "ORDER BY r.created_at DESC")
    List<ReviewDTO> selectReviewsByBookId(@Param("bookId") Integer bookId);

    @Select("SELECT AVG(rating) FROM review WHERE book_id = #{bookId}")
    Double selectAvgRatingByBookId(@Param("bookId") Integer bookId);
}