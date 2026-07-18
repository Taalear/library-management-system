package com.example.library;

import com.example.library.entity.Book;
import com.example.library.mapper.BookMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class BookMapperTest {

    @Autowired
    private BookMapper bookMapper;

    @Test
    public void testInsert() {
        Book book = new Book();
        book.setBookName("测试书籍");
        book.setAuthor("测试作者");
        book.setPrice(new BigDecimal("99.00"));
        book.setStock(10);
        int rows = bookMapper.insert(book);
        System.out.println("插入行数：" + rows);
    }
}