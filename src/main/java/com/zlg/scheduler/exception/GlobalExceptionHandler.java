package com.zlg.scheduler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 *
 * @author linyouru
 */
@ControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler implements MessageSourceAware {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MessageSource messageSource;

    /**
     * 意料之外的异常捕获,避免直接返回500
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrResp> serverError(WebRequest request, Exception exception){
        final String path = getPath(request);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String code = getCode(httpStatus,"");
        final ErrResp resp = ErrResp.builder()
                .timestamp(new Date())
                .status(httpStatus.value())
                .code(code)
                .error(httpStatus.getReasonPhrase())
                .path(path)
                .message("server error")
                .build();
        String errorMessage = buildErrorMessage(exception);
        logger.error(errorMessage);
        return new ResponseEntity<>(resp, httpStatus);
    }

    /**
     * 处理query参数校验不通过的异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<ErrResp> handleMethodArgumentNotValidException(ConstraintViolationException e, WebRequest request) {
        final String path = getPath(request);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String code = getCode(httpStatus,"");
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(constv -> errors.put(String.valueOf(constv.getPropertyPath()),constv.getMessage()));
        final ErrResp resp = ErrResp.builder()
                .timestamp(new Date())
                .status(httpStatus.value())
                .code(code)
                .error(httpStatus.getReasonPhrase())
                .path(path)
                .message(errors)
                .build();
        return new ResponseEntity<>(resp, httpStatus);
    }

    /**
     * 处理 json 请求体调用接口校验失败抛出的异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrResp> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, WebRequest request) {
        final String path = getPath(request);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String code = getCode(httpStatus,"");
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error->errors.put(error.getField(),error.getDefaultMessage()));
        final ErrResp resp = ErrResp.builder()
                .timestamp(new Date())
                .status(httpStatus.value())
                .code(code)
                .error(httpStatus.getReasonPhrase())
                .path(path)
                .message(errors)
                .build();
        return new ResponseEntity<>(resp, httpStatus);
    }

    /**
     * BizException异常处理
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ErrResp> handleBizException(BizException ex, WebRequest request) {
        final String path = getPath(request);
        final String message = getMessage(ex);
        final HttpStatus httpStatus = ex.getHttpStatus();
        final String code = getCode(httpStatus, ex.getBizCode());
        final ErrResp resp = ErrResp.builder()
                .timestamp(new Date())
                .status(httpStatus.value())
                .code(code)
                .error(httpStatus.getReasonPhrase())
                .path(path)
                .message(message).build();
//        logDetails(path, message, httpStatus, code);
        return new ResponseEntity<>(resp, httpStatus);
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private String getMessage(BizException ex) {
        if (!StringUtils.hasText(ex.getBizCode())) {
            return ex.getHttpStatus().getReasonPhrase();
        }
        if (this.messageSource == null) {
            return ex.getBizCode();
        }
        return this.messageSource.getMessage(ex.getBizCode(), ex.getArgs(), ex.getBizCode(), LocaleContextHolder.getLocale());
    }

    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return request.getContextPath();
    }

    private String getCode(HttpStatus status, String bizCode) {
        if (!StringUtils.hasText(bizCode)) {
            return "http." + status.value();
        }
        return bizCode;
    }

    /**
     * 打印异常堆栈信息
     */
    private static String getStackTraceString(Throwable ex){
        StackTraceElement[] traceElements = ex.getStackTrace();
        StringBuilder traceBuilder = new StringBuilder();
        if (traceElements != null && traceElements.length > 0) {
            for (StackTraceElement traceElement : traceElements) {
                traceBuilder.append(traceElement.toString());
                traceBuilder.append("\n");
            }
        }
        return traceBuilder.toString();
    }

    /**
     * 构造异常堆栈信息
     */
    private static String buildErrorMessage(Exception ex) {
        String result;
        String stackTrace = getStackTraceString(ex);
        String exceptionType = ex.toString();
        String exceptionMessage = ex.getMessage();
        result = String.format("%s : %s \r\n %s", exceptionType, exceptionMessage, stackTrace);
        return result;
    }

}
