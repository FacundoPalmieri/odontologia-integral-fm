package com.odontologiaintegralfm.exception;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;


@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AppException extends RuntimeException {

  private final String userMessageKey;
  private final String logMessageKey;
  private final Long id;
  private final String value;
  private final String clase ;
  private final String method;
  private final LogLevel logLevel;
  private final Object[] userArgs;


  public AppException(String message, String userMessageKey, Object[] userArgs, String logMessageKey, Long id, String value, String clase, String method,LogLevel logLevel) {
    super(message);
    this.userMessageKey = userMessageKey;
    this.userArgs = userArgs;
    this.logMessageKey = logMessageKey;
    this.id = id;
    this.value = value;
    this.clase = clase;
    this.method = method;
    this.logLevel = logLevel;
  }

  public abstract HttpStatus getStatus();
}