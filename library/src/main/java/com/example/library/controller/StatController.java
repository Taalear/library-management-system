package com.example.library.controller;

import com.example.library.entity.Result;
import com.example.library.entity.StatOverviewVO;
import com.example.library.entity.TopBookVO;
import com.example.library.entity.TrendVO;
import com.example.library.service.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "统计接口", description = "数据看板统计")
@RestController
@RequestMapping("/api/stat")
public class StatController {

    @Autowired
    private StatService statService;

    @Operation(summary = "统计概览（图书总数、用户总数等）")
    @GetMapping("/overview")
    public Result<StatOverviewVO> getOverview() {
        return Result.success(statService.getOverview());
    }

    @Operation(summary = "近7天借阅趋势")
    @GetMapping("/trend")
    public Result<List<TrendVO>> getTrend() {
        return Result.success(statService.getTrend());
    }

    @Operation(summary = "热门图书 Top 5")
    @GetMapping("/top-books")
    public Result<List<TopBookVO>> getTopBooks(
            @Parameter(description = "时间范围：week/month/all")
            @RequestParam(defaultValue = "all") String range) {
        return Result.success(statService.getTopBooks(range));
    }
}