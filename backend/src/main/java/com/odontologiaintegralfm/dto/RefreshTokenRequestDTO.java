package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

    @NotNull(message = "refreshTokenRequestDTO.refreshEmpty")
    private String refreshToken;

    @NotNull(message = "refreshTokenRequestDTO.userIdEmpty")
    private Long idUser;

    @NotNull(message = "refreshTokenRequestDTO.usernameEmpty")
    private String username;
}