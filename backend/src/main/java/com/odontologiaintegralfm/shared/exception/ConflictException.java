package com.odontologiaintegralfm.shared.exception;

import com.odontologiaintegralfm.shared.enums.LogLevel;
import org.springframework.http.HttpStatus;


public class ConflictException extends AppException {
    public ConflictException(String userMessageKey, Object[] userArgs, String logMessageKey,Object[] logArgs, LogLevel logLevel) {
        super(userMessageKey,userArgs,logMessageKey,logArgs, logLevel);
    }


    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}