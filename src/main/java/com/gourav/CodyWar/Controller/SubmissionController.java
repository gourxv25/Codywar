package com.gourav.CodyWar.Controller;

import com.gourav.CodyWar.Domain.Dto.SubmissionRequestDto;
import com.gourav.CodyWar.Domain.Dto.SubmissionResponseDto;
import com.gourav.CodyWar.Service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    // 🔥 Create Submission (Async execution start)
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SubmissionResponseDto> createSubmission(
            @RequestBody SubmissionRequestDto request) {

        SubmissionResponseDto response = submissionService.createSubmission(request);
        return ResponseEntity.ok(response);
    }

    // 🔥 Poll submission status
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<SubmissionResponseDto> getSubmissionStatus(
            @PathVariable UUID id) {

        SubmissionResponseDto response = submissionService.getSubmissionStatus(id);
        return ResponseEntity.ok(response);
    }
}