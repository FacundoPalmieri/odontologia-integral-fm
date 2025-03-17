package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.RefreshToken;

public interface IRefreshTokenService {

    RefreshToken createRefreshToken(String username);

    boolean validateRefreshToken(String refreshToken, String username);

    void deleteRefreshTokenByUsername(String token,String username);

    RefreshToken getRefreshTokenByUsername(String token, String username);

}