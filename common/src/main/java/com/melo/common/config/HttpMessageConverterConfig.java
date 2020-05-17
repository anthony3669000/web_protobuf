package com.melo.common.config;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class HttpMessageConverterConfig {
    /**
     * 构造ProtobufHttpMessageConverter
     * @return
     */
    private ProtobufHttpMessageConverter protobufHttpMessageConverter(){
        ProtobufHttpMessageConverter protobufHttpMessageConverter = new ProtobufHttpMessageConverter();
        //List<MediaType> supportedMediaTypes = new ArrayList<>();
        //supportedMediaTypes.add(ProtobufHttpMessageConverter.PROTOBUF);
        //protobufHttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
        return protobufHttpMessageConverter;
    }

    /**
     * HttpMessageConverters
     * @return
     */
    @Bean
    public HttpMessageConverters httpMessageConverters(){
        return new HttpMessageConverters(protobufHttpMessageConverter());
    }
}
