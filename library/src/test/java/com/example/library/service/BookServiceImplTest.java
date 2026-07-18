package com.example.library.service;

import com.example.library.entity.Book;
import com.example.library.exception.BusinessException;
import com.example.library.mapper.BookMapper;
import com.example.library.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1);
        testBook.setBookName("测试图书");
        testBook.setAuthor("测试作者");
        testBook.setPrice(new BigDecimal("29.99"));
        testBook.setStock(10);
    }

    @Test
    void testAddBook_Success() {
        when(bookMapper.insert(any(Book.class))).thenReturn(1);
        assertDoesNotThrow(() -> bookService.addBook(testBook));
        verify(bookMapper, times(1)).insert(testBook);
    }

    @Test
    void testDeleteBook_NotFound_ThrowsException() {
        when(bookMapper.selectById(1)).thenReturn(null);
        assertThrows(BusinessException.class, () -> bookService.deleteBook(1));
    }

    @Test
    void testDeleteBook_Success() {
        when(bookMapper.selectById(1)).thenReturn(testBook);
        when(bookMapper.deleteById(1)).thenReturn(1);
        assertDoesNotThrow(() -> bookService.deleteBook(1));
        verify(bookMapper, times(1)).deleteById(1);
    }
}