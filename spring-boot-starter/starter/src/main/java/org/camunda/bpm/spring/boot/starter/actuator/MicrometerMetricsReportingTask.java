/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

