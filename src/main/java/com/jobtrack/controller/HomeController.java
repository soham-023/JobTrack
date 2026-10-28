package com.jobtrack.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Hidden
Public class Homecontroller{
   
public class HomeController {

    @GetMapping("/")
    public String home() {
        
        return "forward:/index.html";
    }
}
