package com.arenabast.api.dto;

import lombok.Data;

@Data
public class ResponseWrapper<T> {
    private int code;
    private String message;
    private boolean status;
    private T data;

    public ResponseWrapper(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.status = code == 200;
    }

    public ResponseWrapper(boolean status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ResponseWrapper(boolean status, int code, T data) {
        this.status = status;
        this.message = message;
        this.code = code;
        this.data = data;
    }

    public ResponseWrapper(boolean status, int code, String message, T data) {
        this.status = status;
        this.message = message;
        this.code = code;
        this.data = data;
    }

}