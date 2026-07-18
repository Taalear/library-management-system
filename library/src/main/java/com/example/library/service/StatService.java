package com.example.library.service;

import com.example.library.entity.StatOverviewVO;
import com.example.library.entity.TopBookVO;
import com.example.library.entity.TrendVO;

import java.util.List;

public interface StatService {
    StatOverviewVO getOverview();
    List<TrendVO> getTrend();
    List<TopBookVO> getTopBooks(String range);  // 新增 range 参数
}