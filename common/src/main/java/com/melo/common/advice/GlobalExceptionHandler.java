package com.melo.common.advice;

import com.alibaba.fastjson.JSONObject;
import com.melo.common.enums.ResponseStatus;
import com.melo.model.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.AccountNotFoundException;
import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String PROTOBUF = "application/x-protobuf";
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e, HttpServletRequest request){
        return processResult(ResponseStatus.FAILED,e.getMessage(),request);
    }
    @ExceptionHandler(AccountNotFoundException.class)
    public Object accountNotFoundExceptionHandler(AccountNotFoundException e, HttpServletRequest request){
        return processResult(ResponseStatus.LOGIN_FAILED,request);
    }

    @ExceptionHandler(Exception.class)
    public Object exceptionHandler(Exception e, HttpServletRequest request){
        return processResult(ResponseStatus.FAILED,request);
    }

    private Object processResult(ResponseStatus responseStatus, HttpServletRequest request){
        return processResult(responseStatus,null,request);
    }

    private Object processResult(ResponseStatus responseStatus, String message, HttpServletRequest request){
        String contentType = request.getHeader("Content-Type");
        message = StringUtils.isEmpty(message)?responseStatus.getMessage(): message;
        if(StringUtils.startsWith(contentType,PROTOBUF)){
            Result.Response response = Result.Response.newBuilder()
                    .setCode(responseStatus.getCode())
                    .setMessage(message)
                    .build();
            return response;
        } else {
            JSONObject response = new JSONObject();
            response.put("code",responseStatus.getCode());
            response.put("message",message);
            return response;
        }
    }
}
