package com.melo.web.controller;

import com.melo.common.annotation.ProtoBufRestController;
import com.melo.model.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;

import javax.security.auth.login.AccountNotFoundException;

@ProtoBufRestController("/login")
public class LoginController {
    private static final Logger logger =  LoggerFactory.getLogger(LoginController.class);

    @PostMapping
    public Login.LoginResponse login(Login.LoginRequest request) throws Exception {
        logger.info("username {} login password {}",new Object[]{request.getUsername(),request.getPassword()});
        if(!"admin".equals(request.getUsername())||!"admin".equals(request.getPassword())){
            Login.LoginResponse response = Login.LoginResponse.newBuilder().setVersion(System.currentTimeMillis()).build();
            return response;
        }
        throw  new AccountNotFoundException("用户名或密码不正确！");
    }
}
