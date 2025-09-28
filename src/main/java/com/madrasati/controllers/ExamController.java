package com.madrasati.controllers;

import com.madrasati.services.CircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final CircuitBreaker circuitBreaker = new CircuitBreaker(3, Duration.ofSeconds(10));

    @PostMapping("/submit")
    public ResponseEntity<?> submitExam() {
        try {
            String result = circuitBreaker.call(() -> simulatedExamServiceCall());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Exam service unavailable: " + e.getMessage());
        }
    }

    private String simulatedExamServiceCall() {
        // Simulate 30% failure to trip the breaker
        if (Math.random() < 0.3) {
            throw new RuntimeException("Exam backend error");
        }
        return "exam submitted";
    }
}
