package ru.red.lazaruscloud.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/home")
    public String home(){
        return "forward:/index.html";
    }

    @GetMapping("/auth")
    public String auth(){
        return "forward:/auth.html";
    }

    @GetMapping("/")
    public String base(){
        return "forward:/auth";
    }
}
