package com.example.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.entity.Book;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.BorrowRecordDTO;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Override
    @Transactional
    public void borrowBook(Integer userId, Integer bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        LambdaQueryWrapper<BorrowRecord> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getBookId, bookId)
                .eq(BorrowRecord::getStatus, 0);
        if (borrowRecordMapper.selectOne(checkWrapper) != null) {
            throw new BusinessException(ErrorCode.BOOK_ALREADY_BORROWED);
        }
        int rows = bookMapper.decreaseStock(bookId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.BOOK_STOCK_INSUFFICIENT);
        }
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setStatus(0);
        record.setBorrowTime(LocalDateTime.now());
        borrowRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public void returnBook(Integer userId, Integer bookId) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getBookId, bookId)
                .eq(BorrowRecord::getStatus, 0)
                .last("limit 1");
        BorrowRecord record = borrowRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND);
        }
        bookMapper.increaseStock(bookId);
        record.setStatus(1);
        record.setReturnTime(LocalDateTime.now());
        borrowRecordMapper.updateById(record);
    }

    @Override
    public List<BorrowRecordDTO> getMyBorrowRecords(Integer userId) {
        return borrowRecordMapper.selectBorrowRecordsByUserId(userId);
    }

    @Override
    public List<BorrowRecordDTO> getOverdueBorrows(Integer userId) {
        LocalDateTime overdueDate = LocalDateTime.now().minusDays(28);
        return borrowRecordMapper.selectOverdueBorrows(userId, overdueDate);
    }
}