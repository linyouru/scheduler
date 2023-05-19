package com.zlg.scheduler.exception;

import java.util.Date;

/**
 * 错误返回实体
 */
public class ErrResp {
    private final Date timestamp;
    private final int status;
    private final String path;
    private final String code;
    private final String error;
    private final Object message;

    public ErrResp(Date timestamp, int status, String path, String code, String error, Object message) {
        this.timestamp = timestamp;
        this.status = status;
        this.path = path;
        this.code = code;
        this.error = error;
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrResp{" +
                "timestamp=" + timestamp +
                ", status=" + status +
                ", path='" + path + '\'' +
                ", code='" + code + '\'' +
                ", error='" + error + '\'' +
                ", message=" + message +
                '}';
    }

    public static ErrRespBuilder builder() {
        return new ErrRespBuilder();
    }

    public static class ErrRespBuilder {
        private Date timestamp;
        private int status;
        private String path;
        private String code;
        private String error;
        private Object message;

        ErrRespBuilder() {
        }

        public ErrRespBuilder timestamp(final Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrRespBuilder status(final int status) {
            this.status = status;
            return this;
        }

        public ErrRespBuilder path(final String path) {
            this.path = path;
            return this;
        }

        public ErrRespBuilder code(final String code) {
            this.code = code;
            return this;
        }

        public ErrRespBuilder error(final String error) {
            this.error = error;
            return this;
        }

        public ErrRespBuilder message(final Object message) {
            this.message = message;
            return this;
        }

        public ErrResp build() {
            return new ErrResp(this.timestamp, this.status, this.path, this.code, this.error, this.message);
        }

        public String toString() {
            return "ErrResp.ErrRespBuilder(timestamp=" + this.timestamp + ", status=" + this.status + ", path=" + this.path + ", code=" + this.code + ", error=" + this.error + ", message=" + this.message + ")";
        }
    }
}
