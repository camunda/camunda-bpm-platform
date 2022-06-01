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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.scheduling.annotation.Scheduled;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

public class CamundaMonitoringMetrics {

  private final ProcessEngine processEngine;
  private final MeterRegistry registry;
  private final Map<Pair<String, List<Tag>>, AtomicLong> metrics;

  public CamundaMonitoringMetrics(final MeterRegistry meterRegistry, final ProcessEngine engine) {
    this.processEngine = requireNonNull(engine);
    this.registry = requireNonNull(meterRegistry);
    this.metrics = new HashMap<>();
  }

  @Scheduled(fixedRate = 10000L)
  void getMetrics(){
    processEngine.getManagementService().getMetrics().forEach(sampleFamily -> {
      sampleFamily.getSamples().values().forEach(sample -> {
        // collect tags
        List<String> labelNames = sample.getLabelNames();
        List<String> labelValues = sample.getLabelValues();
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < labelNames.size(); i++) {
          tags.add(Tag.of(labelNames.get(i), labelValues.get(i)));
        }
        Pair<String, List<Tag>> metricKey = Pair.of(sampleFamily.getName(), tags);
        AtomicLong atomicLong = metrics.get(metricKey);
        if (atomicLong == null) {
          // add to internal metrics map
          metrics.put(Pair.of(sampleFamily.getName(), tags),
              registry.gauge(sampleFamily.getName(), tags, new AtomicLong(sample.getValue())));
        } else {
          atomicLong.set(sample.getValue());
        }
      });
    });
  }
}
