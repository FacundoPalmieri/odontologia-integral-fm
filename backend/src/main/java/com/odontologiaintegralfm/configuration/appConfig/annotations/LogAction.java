package com.odontologiaintegralfm.configuration.appConfig.annotations;


import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.enums.LogType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)          // Se usa para métodos
@Retention(RetentionPolicy.RUNTIME)  // Disponible en tiempo de ejecución
public @interface LogAction {
    String   value()  default "";     // Mensaje personalizado
    String[] args ()  default {};
    LogType  type ()  default LogType.SYSTEM;
    LogLevel level()  default LogLevel.INFO;


}
