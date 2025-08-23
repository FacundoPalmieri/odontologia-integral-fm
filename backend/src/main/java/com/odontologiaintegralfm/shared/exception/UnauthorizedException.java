package com.odontologiaintegralfm.shared.exception;


import com.odontologiaintegralfm.shared.enums.LogLevel;
import org.springframework.http.HttpStatus;


public class UnauthorizedException extends AppException {
  public UnauthorizedException(String userMessageKey, Object[] userArgs, String logMessageKey,Object[] logArgs, LogLevel logLevel) {
    super(userMessageKey,userArgs,logMessageKey,logArgs, logLevel);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.UNAUTHORIZED;
  }
}