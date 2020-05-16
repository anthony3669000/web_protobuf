package com.melo.common.advice;
import com.google.protobuf.GeneratedMessageV3;
import com.melo.common.annotation.ProtoBufRestController;
import com.melo.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import com.melo.common.enums.ResponseStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
public class ProtoBufResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger =  LoggerFactory.getLogger(ProtoBufResponseBodyAdvice.class);

    private static final MediaType PROTOBUF_UTF = new MediaType("application","x-protobuf", StandardCharsets.UTF_8);

    private static final MediaType PROTOBUF = new MediaType("application","x-protobuf");

    private static  String serverIP = null;

    static {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            String hostname = localhost.getHostName();
            String hostAddress = localhost.getHostAddress();
            serverIP = hostname + "_" + hostAddress;
        } catch (UnknownHostException e) {
            logger.warn("unknown host exception",e);
        }
    }

    /**
     * 判断是否需要处理
     * @param methodParameter
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        Boolean isHasPbResultControllerClassAnnotion = methodParameter.getDeclaringClass().isAnnotationPresent(ProtoBufRestController.class);
        return isHasPbResultControllerClassAnnotion;
    }

    /**
     * 处理返回内容
     * @param body
     * @param methodParameter
     * @param selectedType
     * @param aClass
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType selectedType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        Result.Response.Builder builder = null;
        if(PROTOBUF.equals(selectedType) || PROTOBUF_UTF.equals(selectedType)){
            if(body == null || !(body instanceof Result.Response)){
                com.google.protobuf.GeneratedMessageV3 data = (GeneratedMessageV3) body;
                builder = Result.Response.newBuilder()
                        .setCode(ResponseStatus.SUCCESSFUL.getCode())
                        .setMessage(ResponseStatus.SUCCESSFUL.getMessage()).setData(body == null ? null: data.toByteString());
            } else {
                builder = Result.Response.newBuilder((Result.Response) body);
            }
            builder.setTimestamp(System.currentTimeMillis());
            builder.setServerIP(serverIP);
            body = builder.build();
        }
        return body;
    }
}