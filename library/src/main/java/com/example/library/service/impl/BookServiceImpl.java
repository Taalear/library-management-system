package com.example.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.Book;
import com.example.library.entity.BookWithCategoryDTO;
import com.example.library.entity.Category;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.CategoryMapper;
import com.example.library.service.BookService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void addBook(Book book) {
        bookMapper.insert(book);
    }

    @Override
    public void deleteBook(Integer id) {
        if (bookMapper.selectById(id) == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        bookMapper.deleteById(id);
    }

    @Override
    public void updateBook(Book book) {
        if (book.getId() == null || bookMapper.selectById(book.getId()) == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        bookMapper.updateById(book);
    }

    @Override
    public Page<BookWithCategoryDTO> getBookPage(Integer pageNum, Integer pageSize, String bookName, Integer categoryId) {
        Page<BookWithCategoryDTO> page = new Page<>(pageNum, pageSize);
        return bookMapper.selectBookPageWithCategory(page, bookName, categoryId);
    }

    @Override
    public BookWithCategoryDTO getBookById(Integer id) {
        BookWithCategoryDTO dto = bookMapper.selectBookWithCategoryById(id);
        if (dto == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        return dto;
    }

    @Override
    public List<String> suggestBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Book::getBookName, keyword.trim())
                .orderByAsc(Book::getBookName)
                .last("limit 10");
        List<Book> books = bookMapper.selectList(wrapper);
        return books.stream().map(Book::getBookName).collect(Collectors.toList());
    }

    // ===== 同分类推荐 =====
    @Override
    public List<BookWithCategoryDTO> getRecommendBooks(Integer bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getCategoryId() == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getCategoryId, book.getCategoryId())
                .ne(Book::getId, bookId)
                .orderByDesc(Book::getStock)
                .last("limit 4");
        List<Book> books = bookMapper.selectList(wrapper);
        List<BookWithCategoryDTO> result = new ArrayList<>();
        for (Book b : books) {
            BookWithCategoryDTO dto = new BookWithCategoryDTO();
            BeanUtils.copyProperties(b, dto);
            if (b.getCategoryId() != null) {
                Category category = categoryMapper.selectById(b.getCategoryId());
                if (category != null) {
                    dto.setCategoryName(category.getName());
                }
            }
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<BookWithCategoryDTO> getAllBooksForExport() {
        Page<BookWithCategoryDTO> page = new Page<>(1, 9999);
        return bookMapper.selectBookPageWithCategory(page, null, null).getRecords();
    }
}