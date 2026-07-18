package com.example.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.entity.*;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.service.StatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatServiceImpl implements StatService {

    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BorrowRecordMapper borrowRecordMapper;

    @Override
    public StatOverviewVO getOverview() {
        StatOverviewVO vo = new StatOverviewVO();
        vo.setBookCount(bookMapper.selectCount(null));
        vo.setUserCount(userMapper.selectCount(null));
        vo.setBorrowCount(borrowRecordMapper.selectCount(null));

        LambdaQueryWrapper<BorrowRecord> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(BorrowRecord::getStatus, 0);
        vo.setActiveBorrowCount(borrowRecordMapper.selectCount(activeWrapper));
        return vo;
    }

    @Override
    public List<TrendVO> getTrend() {
        List<String> dateList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dateList.add(date.format(formatter));
        }

        List<Map<String, Object>> dbResult = borrowRecordMapper.selectTrendData();
        Map<String, Long> dataMap = new HashMap<>();
        if (dbResult != null) {
            for (Map<String, Object> row : dbResult) {
                String date = (String) row.get("date");
                Long count = (Long) row.get("count");
                if (dateList.contains(date)) {
                    dataMap.put(date, count);
                }
            }
        }

        List<TrendVO> result = new ArrayList<>();
        for (String date : dateList) {
            TrendVO vo = new TrendVO();
            vo.setDate(date);
            vo.setCount(dataMap.getOrDefault(date, 0L));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<TopBookVO> getTopBooks(String range) {
        if ("week".equals(range)) {
            String startDate = LocalDate.now().minusWeeks(1).toString();
            return borrowRecordMapper.selectTopBooksByDateRange(startDate);
        } else if ("month".equals(range)) {
            String startDate = LocalDate.now().minusMonths(1).toString();
            return borrowRecordMapper.selectTopBooksByDateRange(startDate);
        } else {
            return borrowRecordMapper.selectTopBooks();
        }
    }
}