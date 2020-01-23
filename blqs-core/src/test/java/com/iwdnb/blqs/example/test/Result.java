package com.iwdnb.blqs.example.test;

import java.io.Serializable;

public class Result<T> implements Serializable {

    public static final String SUCCESS_CODE = "0";
    public static final String ERROR_CODE   = "-1";

    private String             code;

    private T                  data;

    private boolean            isSuccess    = true;

    private String             message;

    public Result(){
    }

    public Result(boolean isSuccess){
        this.isSuccess = isSuccess;
    }

    public Result(boolean isSuccess, String message, String code, T retValue){
        super();
        this.isSuccess = isSuccess;
        this.message = message;
        this.code = code;
        this.data = retValue;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean hasError() {
        return !isSuccess;
    }

}
