package com.gourav.CodyWar.Domain.Dto;

import lombok.Data;

@Data
public class SubmissionRequestDto {

    // Required: kis problem ke liye submit ho raha hai
    private Long problemId;

    // Battle mode me use hoga (practice me null ho sakta hai)
    private Long battleId;

    // User ka source code (DB me store nahi karna)
    private String code;

    // Execution language (JAVA, PYTHON, CPP, etc.)
    private String language;
}