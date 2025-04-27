package com.odontologiaintegralfm.exception;

import com.odontologiaintegralfm.enums.LogLevel;
import org.springframework.http.HttpStatus;


public class ConflictException extends AppException {
    public ConflictException(String message, String userMessageKey, Object[] userArgs, String logMessageKey, Long id, String value, String clase, String method, LogLevel logLevel) {
        super(message, userMessageKey,userArgs,logMessageKey, id, value, clase, method, logLevel);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}