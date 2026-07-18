package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.BorrowRecordDTO;
import com.example.library.entity.TopBookVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    @Select("SELECT " +
            "br.id, br.user_id, br.book_id, br.borrow_time, br.return_time, br.status, " +
            "u.username, " +
            "b.book_name, b.author " +
            "FROM borrow_record br " +
            "LEFT JOIN user u ON br.user_id = u.id " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "WHERE br.user_id = #{userId} " +
            "ORDER BY br.borrow_time DESC")
    List<BorrowRecordDTO> selectBorrowRecordsByUserId(@Param("userId") Integer userId);

    @Select("SELECT " +
            "br.id, br.user_id, br.book_id, br.borrow_time, br.return_time, br.status, " +
            "u.username, " +
            "b.book_name, b.author " +
            "FROM borrow_record br " +
            "LEFT JOIN user u ON br.user_id = u.id " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "ORDER BY br.borrow_time DESC " +
            "LIMIT #{limit}")
    List<BorrowRecordDTO> selectRecentBorrows(@Param("limit") Integer limit);

    @Select("SELECT DATE_FORMAT(borrow_time, '%m-%d') as date, COUNT(*) as count " +
            "FROM borrow_record " +
            "WHERE borrow_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(borrow_time, '%m-%d') " +
            "ORDER BY date ASC")
    List<Map<String, Object>> selectTrendData();

    // 原无参方法
    @Select("SELECT b.id, b.book_name as bookName, b.author, b.price, b.stock, b.description, " +
            "c.name as categoryName, COUNT(br.id) as borrowCount " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "GROUP BY br.book_id " +
            "ORDER BY borrowCount DESC " +
            "LIMIT 5")
    List<TopBookVO> selectTopBooks();

    // 新增按时间范围查询
    @Select("SELECT b.id, b.book_name as bookName, b.author, b.price, b.stock, b.description, " +
            "c.name as categoryName, COUNT(br.id) as borrowCount " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "WHERE br.borrow_time >= #{startDate} " +
            "GROUP BY br.book_id " +
            "ORDER BY borrowCount DESC " +
            "LIMIT 5")
    List<TopBookVO> selectTopBooksByDateRange(@Param("startDate") String startDate);

    @Select("SELECT br.id, br.book_id, br.borrow_time, " +
            "b.book_name, b.author " +
            "FROM borrow_record br " +
            "LEFT JOIN book b ON br.book_id = b.id " +
            "WHERE br.user_id = #{userId} " +
            "AND br.status = 0 " +
            "AND br.borrow_time < #{overdueDate}")
    List<BorrowRecordDTO> selectOverdueBorrows(@Param("userId") Integer userId,
                                               @Param("overdueDate") LocalDateTime overdueDate);
}