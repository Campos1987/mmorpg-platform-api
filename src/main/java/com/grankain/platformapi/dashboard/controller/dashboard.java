package com.grankain.platformapi.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class dashboard {

    @PostMapping("/")
    public ResponseEntity<String> userStart() {
        return ResponseEntity.ok("Dados confidenciais do sistema.");
    }
}
