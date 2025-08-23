package com.odontologiaintegralfm.infrastructure.scheduler.config;

import com.odontologiaintegralfm.shared.enums.ScheduledTaskKey;
import com.odontologiaintegralfm.infrastructure.scheduler.task.CleanupService;
import com.odontologiaintegralfm.infrastructure.scheduler.service.IScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import java.util.Date;

/**
 * Clase que se activa al iniciar Spring.
 * <p>
 * Permite registrar tareas programadas de forma dinámica en tiempo de ejecución usando {@link ScheduledTaskRegistrar}.
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
@Slf4j
@Configuration
@EnableScheduling // Habilita la ejecución de tareas programadas en el contexto de Spring
public class SchedulerInitializerConfig implements SchedulingConfigurer {

    @Autowired
    private CleanupService cleanupService;
    @Autowired
    private IScheduleService scheduleConfigService;

    /**
     * Método obligatorio al implementar SchedulingConfigurer.
     * Permite registrar tareas programadas dinámicamente en tiempo de ejecución.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        // Registrar tareas programadas dinámicamente
        registerTask(taskRegistrar, ScheduledTaskKey.CLEAN_ORPHAN_ADDRESS, () -> cleanupService.cleanOrphanDataAddress());
        registerTask(taskRegistrar, ScheduledTaskKey.CLEAN_ORPHAN_EMAIL, () -> cleanupService.cleanOrphanDataContactEmail());
        registerTask(taskRegistrar, ScheduledTaskKey.CLEAN_ORPHAN_PHONE, () -> cleanupService.cleanOrphanDataContactPhone());
        registerTask(taskRegistrar, ScheduledTaskKey.CLEAN_ATTACHED_FILE_DISABLED, () -> cleanupService.cleanAttachedFileDisabled());
        registerTask(taskRegistrar,ScheduledTaskKey.CLEAN_LOGS, () -> cleanupService.cleanLogs());
    }


    private void registerTask(ScheduledTaskRegistrar registrar, ScheduledTaskKey key, Runnable task) {
        registrar.addTriggerTask(
                task,
                triggerContext -> {
                    try {
                        // 1. Obtener la expresión cron actual desde el servicio
                        String cron = scheduleConfigService.getByKeyName(key);

                        // 2. Crear un CronTrigger con esa expresión
                        CronTrigger trigger = new CronTrigger(cron);

                        // 3. Calcular la próxima fecha de ejecución (devuelve Date, aunque esté deprecated)
                        Date nextExec = trigger.nextExecutionTime(triggerContext);

                        // 4. Retornar la fecha convertida a Instant ya que lo pide Spring
                        return (nextExec != null) ? nextExec.toInstant() : null;
                    } catch (Exception e) {
                        log.warn("Error al obtener cron para tarea: " + key);
                        e.printStackTrace();
                        return null;
                    }
                }
        );
    }
}

