package com.odontologiaintegralfm.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odontologiaintegralfm.configuration.appConfig.annotations.LogAction;
import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.SystemLogResponseDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.enums.LogType;
import com.odontologiaintegralfm.service.interfaces.ISystemLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.util.*;

@Aspect
@Component
public class LogActionAspect {

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private ISystemLogService systemLogService;

    @Autowired
    private MessageSource messageSource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(logAction)")
    public Object logMethod(ProceedingJoinPoint pjp, LogAction logAction) throws Throwable {

        //Obtiene los argumentos del método principal.
        MethodSignature signature = (MethodSignature) pjp.getSignature();


        // Obtener nombres de parámetros y valores reales
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = pjp.getArgs();

        // Crear el contexto de evaluación de SpEL
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], paramValues[i]);  // Convierte todos los argumentos del método interceptado en variables del contexto SpEL
        }

        //Lectura de valores de la anotación @LogAction
        LogLevel level = logAction.level();
        LogType type = logAction.type();

        // Ejecuta el método original y capturar resultado o excepción
        Object result = null;


        // Ejecuta el método original, si falla loguea la exception
        try {
            result = pjp.proceed(); // llama al método original anotado.
            // Agregar resultado al contexto para evaluar en SpEL
            context.setVariable("result", result);

        } catch (Throwable ex) {
            level = LogLevel.ERROR;

            // Guardar log de error en BD y luego re-lanzar la excepción para que se maneje arriba
            systemLogService.save(new SystemLogResponseDTO(
                    level,
                    type,
                    null,
                   "Error al ejecutar el método principal en LogActionAspect.",
                    "LogActionAspect",
                    authenticatedUserService.getAuthenticatedUser().getUsername(),
                    null,
                    systemLogService.getStackTraceAsString(ex)
            ));

            throw ex;
        }

        // Evaluar expresiones definidas en la anotación
        List<Object> resolvedArgs = new ArrayList<>();
        ExpressionParser parser = new SpelExpressionParser();

        // Itera sobre los args declarados en @LogAction.args()
        for (String argExpr : logAction.args()) {
            Object value = parser.parseExpression(argExpr).getValue(context); //Evalúa cada expresión pasada en LogAction contra el contexto cargado de todos los argumentos del método principal, resolviendo el valor real de cada una. Ej Anotación Cuando el parser ve #usuario.nombre busca el objeto usuario y llama a su método getNombre() para devolver "Juan".
            resolvedArgs.add(value);
        }

        // Obtener mensaje
        String message = messageSource.getMessage(
                logAction.value(), // clave del mensaje
                resolvedArgs.toArray(), // args
                LocaleContextHolder.getLocale()
        );

        // Guardar log exitoso en BD
        systemLogService.save(new SystemLogResponseDTO(
                level,
                type,
                null,
                message,
                "LogActionAspect",
                authenticatedUserService.getAuthenticatedUser().getUsername(),
                null,
                null
        ));

        return result; //Devuelve el resultado al método original para que continue el flujo.
    }
}
