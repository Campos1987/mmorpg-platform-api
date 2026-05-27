package com.grankain.platformapi.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class Dashboard {

    @PostMapping("/user/me")
    public ResponseEntity<String> userStart() {
        return ResponseEntity.ok("Hello World");
        
    }
}
