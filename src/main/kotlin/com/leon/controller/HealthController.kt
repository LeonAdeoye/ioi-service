package com.leon.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class HealthController
{
    @GetMapping("/health")
    fun health(): ResponseEntity<String>
    {
        return ResponseEntity.ok("Up")
    }
}
