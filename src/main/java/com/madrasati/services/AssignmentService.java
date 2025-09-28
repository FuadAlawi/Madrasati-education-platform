package com.madrasati.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

@Service
public class AssignmentService {

    private static final Set<String> ALLOWED_EXT = Set.of("pdf","doc","docx","ppt","pptx","zip","txt");
    private static final int MAX_FILENAME = 128;
    private static final long MAX_CONTENT_BYTES = 10L * 1024L * 1024L; // 10 MB

    public record Submission(
            @NotBlank String studentId,
            @NotBlank String courseId,
            @NotBlank @Size(max = MAX_FILENAME) String filename,
            @NotBlank String content // allow raw text or base64 data
    ) {}

    public record SubmissionResult(boolean accepted, String message) {}

    public SubmissionResult validateAndStore(Submission s) {
        // basic null/blank checks are covered by annotations; extra checks below
        if (s.studentId().length() > 64 || s.courseId().length() > 64) {
            return new SubmissionResult(false, "studentId/courseId too long");
        }

        String ext = extractExt(s.filename());
        if (!ALLOWED_EXT.contains(ext)) {
            return new SubmissionResult(false, "file extension not allowed: " + ext);
        }
        if (s.filename().contains("..") || s.filename().contains("/")) {
            return new SubmissionResult(false, "invalid filename");
        }

        byte[] bytes = decodeContent(s.content());
        if (bytes == null) {
            return new SubmissionResult(false, "content must be raw text or base64-encoded");
        }
        if (bytes.length == 0) {
            return new SubmissionResult(false, "empty content");
        }
        if (bytes.length > MAX_CONTENT_BYTES) {
            return new SubmissionResult(false, "file too large; max 10MB");
        }

        // In a real system, store to object storage and persist metadata
        // Here we just simulate success
        return new SubmissionResult(true, "submission accepted");
    }

    private static String extractExt(String filename) {
        int i = filename.lastIndexOf('.') + 1;
        return i > 0 && i < filename.length() ? filename.substring(i).toLowerCase() : "";
    }

    private static byte[] decodeContent(String content) {
        try {
            // Try base64
            return Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            // Fallback to direct bytes of string
            return content.getBytes(StandardCharsets.UTF_8);
        }
    }
}
