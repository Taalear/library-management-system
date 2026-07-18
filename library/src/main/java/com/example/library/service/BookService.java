package com.example.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.Book;
import com.example.library.entity.BookWithCategoryDTO;

import java.util.List;

public interface BookService {
    void addBook(Book book);
    void deleteBook(Integer id);
    void updateBook(Book book);
    Page<BookWithCategoryDTO> getBookPage(Integer pageNum, Integer pageSize, String bookName, Integer categoryId);
    BookWithCategoryDTO getBookById(Integer id);
    List<String> suggestBooks(String keyword);  // ← 新增
    List<BookWithCategoryDTO> getRecommendBooks(Integer bookId);
    List<BookWithCategoryDTO> getAllBooksForExport();

}