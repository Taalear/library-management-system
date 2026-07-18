package com.example.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.entity.OperationLog;
import com.example.library.mapper.OperationLogMapper;
import com.example.library.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void log(String operationType, String target, String details) {
        OperationLog log = new OperationLog();
        log.setOperationType(operationType);
        log.setTarget(target);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.setOperator("anonymousUser".equals(username) ? "系统" : username);

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        log.setIpAddress(ip);

        operationLogMapper.insert(log);
    }

    @Override
    public Page<OperationLog> getLogPage(Integer pageNum, Integer pageSize, String operator, String operationType, String startDate, String endDate) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(operator)) {
            wrapper.like(OperationLog::getOperator, operator);
        }
        if (StringUtils.hasText(operationType)) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        if (StringUtils.hasText(startDate)) {
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.ge(OperationLog::getCreatedAt, start);
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.le(OperationLog::getCreatedAt, end);
        }

        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return operationLogMapper.selectPage(page, wrapper);
    }
}