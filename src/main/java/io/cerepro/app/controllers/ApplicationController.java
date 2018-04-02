package io.cerepro.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class ApplicationController {

    private String testMessage = "Welcome to CerePro!";

    @GetMapping("/")
    public String welcome(Map<String, Object> model) {
        model.put("message", this.testMessage);
        return "welcome";
    }
}
