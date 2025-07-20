package ru.red.lazaruscloud.controller;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FrontendController {
    @GetMapping("/auth")
    public String auth(){
        return "auth";
    }

    @GetMapping("/")
    public String base(){
        return "auth";
    }

    @GetMapping("/home")
    public String home(){
        return "index";
    }
}
