package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.Book;
import com.example.library.entity.BookWithCategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    @Update("update book set stock = stock - 1 where id = #{id} and stock > 0")
    int decreaseStock(@Param("id") Integer id);

    @Update("update book set stock = stock + 1 where id = #{id}")
    int increaseStock(@Param("id") Integer id);

    /**
     * 分页查询图书列表（关联分类表）
     */
    @Select("SELECT b.id, b.book_name, b.author, b.price, b.stock, b.description, b.category_id, " +
            "c.name as category_name " +
            "FROM book b " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "WHERE (#{bookName} IS NULL OR b.book_name LIKE CONCAT('%', #{bookName}, '%')) " +
            "AND (#{categoryId} IS NULL OR b.category_id = #{categoryId}) " +
            "ORDER BY b.id DESC")
    Page<BookWithCategoryDTO> selectBookPageWithCategory(
            Page<BookWithCategoryDTO> page,
            @Param("bookName") String bookName,
            @Param("categoryId") Integer categoryId
    );

    /**
     * 根据ID查询图书详情（含分类名）
     */
    @Select("SELECT b.id, b.book_name, b.author, b.price, b.stock, b.description, b.category_id, " +
            "c.name as category_name " +
            "FROM book b " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "WHERE b.id = #{id}")
    BookWithCategoryDTO selectBookWithCategoryById(@Param("id") Integer id);
}