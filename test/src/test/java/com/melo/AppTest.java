package com.melo;

import static org.junit.Assert.assertTrue;

import com.melo.model.Login;
import com.melo.model.Result;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.melo.common.utils.HttpUtils;

import java.net.URI;

/**
 * Unit test for simple App.
 */

public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public static void login() {
        try {
            URI uri = new URI("http", null, "127.0.0.1", 8080, "/login", "", null);
            HttpPost request = new HttpPost(uri);

            Login.LoginRequest loginRequest = Login.LoginRequest.newBuilder().setUsername("admin").setPassword("admin").build();
            HttpResponse response = HttpUtils.doPost(request, loginRequest);
            Result.Response result = Result.Response.parseFrom(response.getEntity().getContent());
            Login.LoginResponse loginResponse = Login.LoginResponse.parseFrom(result.getData());
            System.err.println("code: " + result.getCode()+ " message: "+ result.getMessage() + " data: "+ loginResponse.getVersion());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        login();
    }
}
