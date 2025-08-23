package com.odontologiaintegralfm.shared.exception;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;


@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AppException extends RuntimeException {

  private final String userMessageKey;
  private final Object[] userArgs;
  private final String logMessageKey;
  private final Object[] logArgs;
  private final LogLevel logLevel;


  public AppException(String userMessageKey, Object[] userArgs, String logMessageKey, Object[] logArgs ,LogLevel logLevel) {
    this.userMessageKey = userMessageKey;
    this.userArgs = userArgs;
    this.logMessageKey = logMessageKey;
    this.logArgs = logArgs;
    this.logLevel = logLevel;
  }

  public abstract HttpStatus getStatus();
}