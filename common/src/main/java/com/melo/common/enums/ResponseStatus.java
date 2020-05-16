package com.melo.common.enums;

public enum ResponseStatus{
    SUCCESSFUL(200,"successful"),
    FAILED(400,"failed"),
    LOGIN_FAILED(401,"username or password error");
    int code;
    String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
