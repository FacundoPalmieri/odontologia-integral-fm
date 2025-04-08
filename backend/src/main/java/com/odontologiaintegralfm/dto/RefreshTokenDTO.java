package com.odontologiaintegralfm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshTokenDTO {

    private String jwt;

    @NotNull(message = "refreshTokenDTO.refreshEmpty")
    private String refreshToken;

    @NotNull(message = "refreshTokenDTO.userIdEmpty")
    private Long idUser;

    @NotNull(message = "refreshTokenDTO.userEmpty")
    private String username;
}