package com.madrasati.controllers;

import com.madrasati.services.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) { this.assignmentService = assignmentService; }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@Valid @RequestBody AssignmentService.Submission submission) {
        var result = assignmentService.validateAndStore(submission);
        if (result.accepted()) return ResponseEntity.ok(result);
        return ResponseEntity.badRequest().body(result);
    }
}
