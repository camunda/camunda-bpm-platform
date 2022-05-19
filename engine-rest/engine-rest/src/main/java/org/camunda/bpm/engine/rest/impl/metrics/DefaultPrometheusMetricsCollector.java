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
package org.camunda.bpm.engine.rest.impl.metrics;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.metrics.MetricSampleFamily;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

/**
 * Default collector for Camunda metrics for Prometheus
 */
public class DefaultPrometheusMetricsCollector extends Collector {

  protected final ProcessEngine processEngine;

  public DefaultPrometheusMetricsCollector(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    Collection<MetricSampleFamily> rawMetricSamples = processEngine.getManagementService().getMetrics();
    return rawMetricSamples.stream().map(this::buildSamples).collect(Collectors.toList());
  }

  private MetricFamilySamples buildSamples(MetricSampleFamily sampleFamily) {
    switch (sampleFamily.type) {
      case COUNTER:
        CounterMetricFamily counterMetricFamily = new CounterMetricFamily(sampleFamily.getName(), sampleFamily.getHelp(), sampleFamily.getLabelNames());
        sampleFamily.getSamples().values().forEach(s -> counterMetricFamily.addMetric(s.getLabelValues(), s.getValue()));
        return counterMetricFamily;
      case GAUGE:
        GaugeMetricFamily gaugeMetricFamily = new GaugeMetricFamily(sampleFamily.getName(), sampleFamily.getHelp(), sampleFamily.getLabelNames());
        sampleFamily.getSamples().values().forEach(s -> gaugeMetricFamily.addMetric(s.getLabelValues(), s.getValue()));
        return gaugeMetricFamily;
    }
    return null;
  }
}
