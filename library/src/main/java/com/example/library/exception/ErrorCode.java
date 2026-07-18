package com.example.library.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 通用系统异常 (1000-1999)
    SYSTEM_ERROR(1000, "系统繁忙，请稍后重试"),
    PARAM_ERROR(1001, "参数校验失败"),

    // 业务异常 (2000-2999)
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_ALREADY_EXISTS(2002, "用户名已存在"),
    PASSWORD_ERROR(2003, "用户名或密码错误"),

    BOOK_NOT_FOUND(2101, "图书不存在"),
    BOOK_STOCK_INSUFFICIENT(2102, "库存不足"),
    BOOK_ALREADY_BORROWED(2103, "您已借阅该书，请先归还"),

    BORROW_RECORD_NOT_FOUND(2201, "借阅记录不存在"),
    ;

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}