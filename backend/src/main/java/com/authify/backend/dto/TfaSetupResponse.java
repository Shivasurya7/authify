package com.authify.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TfaSetupResponse {
    private String secret;
    private String qrCodeUri;
    private String message;
}
