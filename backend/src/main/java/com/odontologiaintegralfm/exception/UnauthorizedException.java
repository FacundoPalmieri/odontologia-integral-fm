package com.odontologiaintegralfm.exception;


import com.odontologiaintegralfm.enums.LogLevel;
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