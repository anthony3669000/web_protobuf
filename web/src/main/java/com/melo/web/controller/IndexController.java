package com.melo.web.controller;

import com.melo.common.annotation.ProtoBufRestController;
import org.springframework.web.bind.annotation.GetMapping;

@ProtoBufRestController
public class IndexController {
    @GetMapping("/index")
    public String index(){
        return "hello";
    }
}
