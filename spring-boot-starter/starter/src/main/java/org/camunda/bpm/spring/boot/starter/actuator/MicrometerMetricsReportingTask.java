package org.camunda.bpm.spring.boot.starter.actuator;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.MetricIntervalValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;


public class MicrometerMetricsReportingTask extends TimerTask {

    private final MeterRegistry meterRegistry;
    private final ProcessEngine processEngine;
    Map<String, Long> metricsMap;

    public MicrometerMetricsReportingTask(MeterRegistry registry, ProcessEngine processEngine) {
        this.meterRegistry = registry;
        this.processEngine = processEngine;
        metricsMap = new HashMap<>();
    }

    public void run() {
        List<MetricIntervalValue> metricsList = processEngine.getManagementService()
                .createMetricsQuery().interval(1);
        if(metricsList.isEmpty()){
            return;
        }
        metricsList.forEach(metric ->{
            if(!metricsMap.containsKey(metric.getName())) {
                Gauge.builder("camunda." + metric.getName(),metricsMap, map -> map.get(metric.getName()))
                        .register(meterRegistry);
            }
            metricsMap.put(metric.getName(), metric.getValue());
        });
    }

}

