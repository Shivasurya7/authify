package com.authify.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyTfaRequest {

    @NotBlank(message = "TFA code is required")
    private String code;
}
