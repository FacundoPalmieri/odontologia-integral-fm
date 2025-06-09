package com.odontologiaintegralfm.configuration.appConfig;

import com.odontologiaintegralfm.scheduled.CleanupService;
import com.odontologiaintegralfm.service.interfaces.IScheduleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import java.util.Date;

/**
 * Método obligatorio al implementar {@link SchedulingConfigurer}.
 * <p>
 * Permite registrar tareas programadas de forma dinámica en tiempo de ejecución usando {@link ScheduledTaskRegistrar}.
 * En este caso, se registra una tarea que ejecuta la limpieza de datos huérfanos, y se programa con una expresión cron obtenida dinámicamente.
 * </p>
 *
 * <h3>Flujo interno de ejecución en Spring</h3>
 * <ol>
 *     <li>Spring levanta la aplicación e invoca este método {@code configureTasks()}.</li>
 *     <li>Se registra una tarea (Runnable) y un trigger (lambda que calcula la próxima ejecución).</li>
 *     <li>Spring llama al trigger inmediatamente para calcular la <b>primera fecha de ejecución</b>:
 *         <ul>
 *             <li>Se obtiene la expresión cron actual desde {@code scheduleConfigService}.</li>
 *             <li>Se crea un {@code CronTrigger} y se calcula la próxima fecha con {@code trigger.nextExecutionTime()}.</li>
 *             <li>La fecha se convierte a {@code Instant} y se guarda internamente.</li>
 *         </ul>
 *     </li>
 *     <li>Spring espera hasta que llegue esa fecha (usa un scheduler interno).</li>
 *     <li>Cuando llega el momento, se ejecuta el {@code Runnable} ({@code cleanupService.cleanOrphanData()}).</li>
 *     <li>Una vez finalizada la tarea, Spring vuelve a llamar al trigger para calcular la <b>próxima fecha de ejecución</b>.</li>
 *     <li>El ciclo se repite indefinidamente: calcular → esperar → ejecutar → calcular → ...</li>
 * </ol>
 *
 * <p>
 * Esto permite cambiar la expresión cron en tiempo de ejecución, sin reiniciar la aplicación.
 * </p>
 */
@Configuration
@EnableScheduling // Habilita la ejecución de tareas programadas en el contexto de Spring
public class DynamicScheduledTaskConfig implements SchedulingConfigurer {

    @Autowired
    private CleanupService cleanupService;
    @Autowired
    private IScheduleConfigService scheduleConfigService;

    /**
     * Método obligatorio al implementar SchedulingConfigurer.
     * Permite registrar tareas programadas dinámicamente en tiempo de ejecución.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                // Primera parte: lógica de la tarea a ejecutar
                () -> cleanupService.cleanOrphanData(),

                // Segunda parte: trigger dinámico (cuándo ejecutar la tarea)
                triggerContext -> {
                    // 1. Obtener la expresión cron actual desde el servicio
                    String cron = scheduleConfigService.getSchedule();

                    // 2. Crear un CronTrigger con esa expresión
                    CronTrigger trigger = new CronTrigger(cron);

                    // 3. Calcular la próxima fecha de ejecución (devuelve Date, aunque esté deprecated)
                    Date nextExec = trigger.nextExecutionTime(triggerContext);

                    // 4. Retornar la fecha convertida a Instant ya que lo pide Spring
                    return (nextExec != null) ? nextExec.toInstant() : null;
                }
        );
    }
}

