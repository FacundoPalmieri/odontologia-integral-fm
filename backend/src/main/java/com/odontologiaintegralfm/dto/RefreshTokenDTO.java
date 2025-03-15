package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshTokenDTO {

    private String token;

    @NotNull(message = "refreshTokenDTO.refreshEmpty")
    private String refreshToken;

    @NotNull(message = "refreshTokenDTO.userEmpty")
    private String user;
}