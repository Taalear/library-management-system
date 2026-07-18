package com.example.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.OperationLog;

public interface OperationLogService {
    // 记录操作日志
    void log(String operationType, String target, String details);

    // 分页查询日志（支持筛选）
    Page<OperationLog> getLogPage(Integer pageNum, Integer pageSize, String operator, String operationType, String startDate, String endDate);
}