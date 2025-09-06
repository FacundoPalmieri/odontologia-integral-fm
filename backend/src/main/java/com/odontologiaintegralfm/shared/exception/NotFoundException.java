package com.odontologiaintegralfm.shared.exception;


import com.odontologiaintegralfm.shared.enums.LogLevel;
import org.springframework.http.HttpStatus;


public class NotFoundException extends AppException {
    public NotFoundException(String userMessageKey, Object[] userArgs, String logMessageKey,Object[] logArgs, LogLevel logLevel) {
        super(userMessageKey,userArgs,logMessageKey,logArgs, logLevel);
    }


    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}