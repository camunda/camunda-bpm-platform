package org.camunda.bpm.spring.boot.starter.actuator;

import jakarta.annotation.PostConstruct;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.MetricIntervalValue;
import org.springframework.beans.factory.annotation.Autowired;
public class MicrometerMetrics {

    @Autowired
    MeterRegistry registry;

    @Autowired
    ProcessEngine processEngine;

    @PostConstruct
    public void init() {
        Map<String, Long> metricsMap = new HashMap<>();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {

            List<MetricIntervalValue> metricsList = processEngine.getManagementService()
                    .createMetricsQuery().interval();
            if(metricsList.isEmpty()){
                return;
            }
            metricsList.forEach(metric ->{
                if(!metricsMap.containsKey(metric.getName())) {
                    Gauge.builder(metric.getName(),metricsMap, map -> map.get(metric.getName()))
                            .register(registry);
                }
                metricsMap.put(metric.getName(), metric.getValue());
            });
        }, 0, 5, TimeUnit.SECONDS);
    }
}
