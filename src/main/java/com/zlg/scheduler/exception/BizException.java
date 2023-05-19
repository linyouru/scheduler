package com.zlg.scheduler.exception;

import com.google.common.base.Strings;
import org.springframework.http.HttpStatus;

/**
 * 自定义异常,通过messages.properties定义的错误码返回对应的错误提示
 */
public class BizException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final String bizCode;
    private final Object[] args;

    public BizException(HttpStatus httpStatus, String bizCode, Object... args) {
        super(Strings.nullToEmpty(bizCode));
        this.httpStatus = httpStatus;
        this.bizCode = bizCode;
        this.args = args;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getBizCode() {
        return bizCode;
    }

    public Object[] getArgs() {
        return args;
    }
}
