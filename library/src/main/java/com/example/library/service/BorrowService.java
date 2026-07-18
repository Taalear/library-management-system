package com.example.library.service;

import com.example.library.entity.BorrowRecordDTO;

import java.util.List;

public interface BorrowService {
    void borrowBook(Integer userId, Integer bookId);
    void returnBook(Integer userId, Integer bookId);
    List<BorrowRecordDTO> getMyBorrowRecords(Integer userId);
    List<BorrowRecordDTO> getOverdueBorrows(Integer userId);
}