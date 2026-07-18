package com.example.library.controller;

import com.example.library.entity.BookWithCategoryDTO;
import com.example.library.entity.BorrowRecordDTO;
import com.example.library.entity.Result;
import com.example.library.entity.User;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.service.BookService;
import com.example.library.service.BorrowService;
import com.example.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Autowired
    private BookService bookService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 借书
    @PostMapping("/{bookId}")
    public Result<String> borrow(@PathVariable Integer bookId) {
        User user = getCurrentUser();
        borrowService.borrowBook(user.getId(), bookId);
        // WebSocket 推送通知
        BookWithCategoryDTO bookDTO = bookService.getBookById(bookId);
        String message = user.getUsername() + " 借阅了《" + bookDTO.getBookName() + "》";
        messagingTemplate.convertAndSend("/topic/notifications", message);
        return Result.success("借书成功！");
    }

    // 还书
    @PutMapping("/return/{bookId}")
    public Result<String> returnBook(@PathVariable Integer bookId) {
        User user = getCurrentUser();
        borrowService.returnBook(user.getId(), bookId);
        // WebSocket 推送通知
        BookWithCategoryDTO bookDTO = bookService.getBookById(bookId);
        String message = user.getUsername() + " 归还了《" + bookDTO.getBookName() + "》";
        messagingTemplate.convertAndSend("/topic/notifications", message);
        return Result.success("还书成功！");
    }

    // 查询我的借阅记录
    @GetMapping("/my")
    public Result<List<BorrowRecordDTO>> getMyBorrowRecords() {
        User user = getCurrentUser();
        return Result.success(borrowService.getMyBorrowRecords(user.getId()));
    }

    // 查询最近借阅记录（用于数据看板）
    @GetMapping("/recent")
    public Result<List<BorrowRecordDTO>> getRecentBorrows(@RequestParam(defaultValue = "5") Integer limit) {
        List<BorrowRecordDTO> list = borrowRecordMapper.selectRecentBorrows(limit);
        return Result.success(list);
    }

    // 查询逾期借阅
    @GetMapping("/overdue")
    public Result<List<BorrowRecordDTO>> getOverdueBorrows() {
        User user = getCurrentUser();
        return Result.success(borrowService.getOverdueBorrows(user.getId()));
    }

    // ===== 工具方法 =====
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在，请重新登录");
        }
        return user;
    }
}