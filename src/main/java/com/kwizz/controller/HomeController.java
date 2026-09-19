package com.kwizz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Principal principal){
        if (principal != null){
            return "redirect:/quizzes";
        }
        return "landing";
    }

}
