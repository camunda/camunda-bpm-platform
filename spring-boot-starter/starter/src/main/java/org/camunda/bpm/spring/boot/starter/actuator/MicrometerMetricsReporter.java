package org.camunda.bpm.spring.boot.starter.actuator;

import jakarta.annotation.PostConstruct;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Timer;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.springframework.beans.factory.annotation.Autowired;

public class MicrometerMetricsReporter {

    @Autowired
    MeterRegistry registry;

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    CamundaBpmProperties camundaBpmProperties;

    private MicrometerMetricsReportingTask micrometerMetricsReportingTask;

    @PostConstruct
    private void init() {
        startReportingCamundaActuatorMetrics();
    }

    private void startReportingCamundaActuatorMetrics() {
        Timer timer = new Timer("Micrometer Camunda Metrics Reporter", true);
        long reportingIntervalInMillis = camundaBpmProperties.getMetrics().getActuator().getInterval() * 1000L;
        micrometerMetricsReportingTask = new MicrometerMetricsReportingTask(registry, processEngine);
        timer.scheduleAtFixedRate(micrometerMetricsReportingTask,
                reportingIntervalInMillis,
                reportingIntervalInMillis);
    }

    public void reportNow() {
        if(micrometerMetricsReportingTask != null) {
            micrometerMetricsReportingTask.run();
        }
    }
}
