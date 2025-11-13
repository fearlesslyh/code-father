package com.lyh.codefather.exception;

import lombok.Getter;

/**
 * @author <a href=https://github.com/fearlesslyh> 梁懿豪 </a>
 * @version 1.0
 * @since 2025/11/10 22:49
 */
@Getter
public enum ErrorCode {
    SUCCESS(0, "ok"),
    PARAMS_ERROR(10000, "参数错误"),
    NOT_LOGIN_ERROR(10001, "未登录"),
    NO_AUTH_ERROR(10002, "无权限"),
    NOT_FOUND_ERROR(10003, "请求数据不存在"),
    FORBIDDEN_ERROR(10004, "禁止访问"),
    SYSTEM_ERROR(10005, "系统内部异常"),
    OPERATION_ERROR(10006, "操作失败");

    private final int code;
    private final String message;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
