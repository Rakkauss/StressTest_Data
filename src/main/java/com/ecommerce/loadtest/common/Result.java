package com.ecommerce.loadtest.common;

/**
 * 统一返回结果类
 * 
 * @author rakkaus
 * @param <T> 数据类型
 */
public class Result<T> {
    
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    private Result(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public static <T> Result<T> success() {
        return new Result<>(0, "操作成功", null);
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "操作成功", data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }
    
    public static <T> Result<T> success(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
    
    public static <T> Result<T> fail() {
        return new Result<>(-1, "操作失败", null);
    }
    
    public static <T> Result<T> fail(String message) {
        return new Result<>(-1, message, null);
    }
    
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    public static <T> Result<T> fail(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
    
    public boolean isSuccess() {
        return this.code != null && this.code == 0;
    }
    
    public boolean isFail() {
        return !isSuccess();
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
