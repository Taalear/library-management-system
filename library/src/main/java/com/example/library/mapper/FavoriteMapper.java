package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.Favorite;
import com.example.library.entity.FavoriteBookDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT f.id as id, f.book_id as bookId, f.created_at as favoritedAt, " +
            "b.book_name as bookName, b.author, b.price, b.stock, " +
            "c.name as categoryName " +
            "FROM favorite f " +
            "LEFT JOIN book b ON f.book_id = b.id " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<FavoriteBookDTO> selectFavoritesByUserId(@Param("userId") Integer userId);
}