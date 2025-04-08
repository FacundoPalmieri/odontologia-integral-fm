package com.odontologiaintegralfm.dto;

import lombok.Data;

@Data
public class RefreshTokenResponseDTO {

    private String jwt;

    private String refreshToken;

    private Long user_id;

    private String username;
}
