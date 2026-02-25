package com.gourav.CodyWar.Service;

import com.gourav.CodyWar.Domain.Dto.SubmissionRequestDto;
import com.gourav.CodyWar.Domain.Dto.SubmissionResponseDto;
import com.gourav.CodyWar.Domain.Entity.*;
import com.gourav.CodyWar.Repository.BattleRepository;
import com.gourav.CodyWar.Repository.SubmissionRepository;
import com.gourav.CodyWar.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Call;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final BattleRepository battleRepository;
    private final UserRepository userRepository;
    private final DockerClientService dockerClientService;

    // 🔥 1️⃣ Create Submission (PENDING + Async Trigger)
    public SubmissionResponseDto createSubmission(SubmissionRequestDto request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Battle battle = battleRepository.findById(request.getBattleId())
                .orElseThrow(() -> new RuntimeException("Battle not found"));

        Submission submission = Submission.builder()
                .battle(battle)
                .user(user)
                .language(Language.valueOf(request.getLanguage()))
                .code(request.getCode())
                .status(SubmissionStatus.PENDING)
                .build();

        Submission savedSubmission = submissionRepository.save(submission);

        // 🔥 Start async execution
        processSubmissionAsync(savedSubmission.getId(), request);


        return SubmissionResponseDto.builder()
                .id(savedSubmission.getId())
                .status(savedSubmission.getStatus())
                .testCasesPassed(0)
                .errorMessage("Submission received. Processing...")
                .build();
    }


    @Async
    public void processSubmissionAsync(UUID submissionId, SubmissionRequestDto request) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setStatus(SubmissionStatus.RUNNING);
        submissionRepository.save(submission);

//        try {
//             🔹 Call Docker execution service (uncomment when ready)
//             DockerExecutionResult result = dockerClientService.execute(request);
//
//             submission.setExecutionTimeMs(result.getExecutionTimeMs());
//             submission.setMemoryUsedKb(result.getMemoryUsedKb());
//             submission.setTestCasesPassed(result.getPassedTestCases());
//             submission.setTotalTestCases(result.getTotalTestCases());
//             submission.setErrorMessage(result.getErrorMessage());
//             submission.setJudgedAt(Instant.now());
//
//             if (result.isSuccess()) {
//                 submission.setStatus(SubmissionStatus.PASSED);
//             } else {
//                 submission.setStatus(SubmissionStatus.FAILED);
//             }
//
//        } catch (Exception e) {
//            submission.setStatus(SubmissionStatus.ERROR);
//            submission.setErrorMessage("Execution failed: " + e.getMessage());
//            submission.setJudgedAt(Instant.now());
//        }

        submissionRepository.save(submission);
    }

    // 🔥 3️⃣ Poll Submission Status
    public SubmissionResponseDto getSubmissionStatus(UUID submissionId) {


        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        int score = 0;

        if (submission.getTotalTestCases() != null
                && submission.getTotalTestCases() > 0) {

            score = (submission.getTestCasesPassed() * 100)
                    / submission.getTotalTestCases();
        }


        return SubmissionResponseDto.builder()
                .id(submission.getId())
                .status(submission.getStatus())
                .testCasesPassed(score)
                .errorMessage(submission.getErrorMessage())
                .build();
    }
}