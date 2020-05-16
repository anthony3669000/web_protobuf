package com.melo.common.utils;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.googlecode.protobuf.format.JsonFormat;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author wangjinliang on 2018/10/18.
 */
public class HttpUtils {

    public static HttpResponse doPost(HttpPost post, GeneratedMessage message) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        String requestUrl = post.getURI().toString();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(message.toByteArray());
        InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream);
        post.setEntity(inputStreamEntity);

        post.addHeader("Content-Type", "application/x-protobuf");
        for (Header header : post.getAllHeaders()) {
            System.out.println(header.getName() + ":" + header.getValue());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("curl -XPOST ");
        for (Header header : post.getAllHeaders()) {
            sb.append(" -H \"").append(header.getName()).append(":").append(header.getValue()).append("\"");
        }

        String jsonBody = JsonFormat.printToString(message);
        jsonBody = jsonBody.replace("\"", "\\\"");
        sb.append(" -d \"").append(jsonBody).append("\"");
        sb.append(" ").append(requestUrl);

        System.out.println(sb.toString());
        return httpclient.execute(post);
    }
    public static HttpResponse doPost(HttpPost post, GeneratedMessageV3 message) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        String requestUrl = post.getURI().toString();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(message.toByteArray());
        InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream);
        post.setEntity(inputStreamEntity);

        post.addHeader("Content-Type", "application/x-protobuf");
        for (Header header : post.getAllHeaders()) {
            System.out.println(header.getName() + ":" + header.getValue());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("curl -XPOST ");
        for (Header header : post.getAllHeaders()) {
            sb.append(" -H \"").append(header.getName()).append(":").append(header.getValue()).append("\"");
        }

        String jsonBody = JsonFormat.printToString(message);
        jsonBody = jsonBody.replace("\"", "\\\"");
        sb.append(" -d \"").append(jsonBody).append("\"");
        sb.append(" ").append(requestUrl);

        System.out.println(sb.toString());
        return httpclient.execute(post);
    }
}
