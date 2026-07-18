package com.example.library.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.Book;
import com.example.library.entity.BookExcelVO;
import com.example.library.entity.BookWithCategoryDTO;
import com.example.library.entity.Result;
import com.example.library.service.BookService;
import com.example.library.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "图书管理", description = "图书的增删改查接口")
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private OperationLogService operationLogService;

    @Operation(summary = "新增图书", description = "管理员专用")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> addBook(@Valid @RequestBody Book book) {
        bookService.addBook(book);
        operationLogService.log("ADD_BOOK", book.getBookName(), "新增图书：" + book.getBookName());
        return Result.success("新增成功");
    }

    @Operation(summary = "删除图书", description = "管理员专用")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteBook(@Parameter(description = "图书ID") @PathVariable Integer id) {
        BookWithCategoryDTO dto = bookService.getBookById(id);
        operationLogService.log("DELETE_BOOK", dto.getBookName(), "删除图书：" + dto.getBookName());
        bookService.deleteBook(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "修改图书", description = "管理员专用")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateBook(@Valid @RequestBody Book book) {
        bookService.updateBook(book);
        operationLogService.log("UPDATE_BOOK", book.getBookName(), "修改图书：" + book.getBookName());
        return Result.success("修改成功");
    }

    @Operation(summary = "分页查询图书", description = "支持按书名模糊搜索和分类筛选")
    @GetMapping
    public Result<Page<BookWithCategoryDTO>> getBooks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "书名关键词") @RequestParam(required = false) String bookName,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId) {
        Page<BookWithCategoryDTO> page = bookService.getBookPage(pageNum, pageSize, bookName, categoryId);
        return Result.success(page);
    }

    @Operation(summary = "根据ID查询图书详情", description = "公开接口")
    @GetMapping("/{id}")
    public Result<BookWithCategoryDTO> getBookById(@Parameter(description = "图书ID") @PathVariable Integer id) {
        BookWithCategoryDTO book = bookService.getBookById(id);
        return Result.success(book);
    }

    @Operation(summary = "搜索补全", description = "输入关键词返回书名建议列表")
    @GetMapping("/suggest")
    public Result<List<String>> suggestBooks(@RequestParam String keyword) {
        List<String> suggestions = bookService.suggestBooks(keyword);
        return Result.success(suggestions);
    }

    @Operation(summary = "获取推荐图书", description = "根据图书ID推荐同分类其他图书")
    @GetMapping("/{id}/recommend")
    public Result<List<BookWithCategoryDTO>> getRecommendBooks(@PathVariable Integer id) {
        List<BookWithCategoryDTO> books = bookService.getRecommendBooks(id);
        return Result.success(books);
    }

    // ===== 批量删除 =====
    @Operation(summary = "批量删除图书", description = "管理员专用")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> batchDeleteBooks(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(1001, "请选择要删除的图书");
        }
        int successCount = 0;
        for (Integer id : ids) {
            try {
                BookWithCategoryDTO dto = bookService.getBookById(id);
                operationLogService.log("DELETE_BOOK", dto.getBookName(), "批量删除图书：" + dto.getBookName());
                bookService.deleteBook(id);
                successCount++;
            } catch (Exception e) {
                // 单个删除失败继续
            }
        }
        return Result.success("批量删除成功，共删除 " + successCount + " 本图书");
    }

    // ===== 导出全部图书 =====
    @Operation(summary = "导出全部图书", description = "管理员专用")
    @GetMapping("/export-all")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportAllBooks(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("全部图书", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<BookWithCategoryDTO> books = bookService.getAllBooksForExport();
        List<BookExcelVO> excelList = books.stream().map(b -> {
            BookExcelVO vo = new BookExcelVO();
            vo.setBookName(b.getBookName());
            vo.setAuthor(b.getAuthor());
            vo.setPrice(b.getPrice());
            vo.setStock(b.getStock());
            vo.setCategoryName(b.getCategoryName());
            return vo;
        }).collect(Collectors.toList());

        EasyExcel.write(response.getOutputStream(), BookExcelVO.class).sheet("全部图书").doWrite(excelList);
    }
}