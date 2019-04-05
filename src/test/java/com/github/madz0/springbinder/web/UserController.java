package com.github.madz0.springbinder.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    public String createUser() {
        return "created";
    }
}
