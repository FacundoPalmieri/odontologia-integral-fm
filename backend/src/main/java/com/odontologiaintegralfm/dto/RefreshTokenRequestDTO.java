package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

    @NotNull(message = "refreshTokenDTO.refreshEmpty")
    private String refreshToken;

    @NotNull(message = "refreshTokenDTO.userIdEmpty")
    private Long user_id;

    @NotNull(message = "refreshTokenDTO.userEmpty")
    private String username;
}