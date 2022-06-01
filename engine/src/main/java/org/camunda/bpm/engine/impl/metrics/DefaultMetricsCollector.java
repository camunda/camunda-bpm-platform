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
package org.camunda.bpm.engine.impl.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.MetricsCollector;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor.JobExecutorThreadMetrics;
import org.camunda.bpm.engine.management.Metrics;

public class DefaultMetricsCollector implements MetricsCollector {

  protected ProcessEngineConfigurationImpl engineConfiguration;

  public DefaultMetricsCollector(ProcessEngineConfigurationImpl engineConfiguration) {
    this.engineConfiguration = engineConfiguration;
  }

  @Override
  public List<MetricSampleFamily> collect() {
    if (engineConfiguration.isMetricsEnabled() && engineConfiguration.isDbMetricsReporterActivate()) {
      ManagementService managementService = engineConfiguration.getManagementService();
      List<MetricSampleFamily> metricsSamples = new ArrayList<>();
      JobExecutorThreadMetrics jobExecutorThreadMetrics = null;
      String reporterId = engineConfiguration.getDbMetricsReporter().getReporterId();
      List<String> labelNames = Arrays.asList("reporter");
      List<String> labelValues = Arrays.asList(reporterId);

      // flush metrics
      managementService.reportDbMetricsNow();

      for (String metricName : engineConfiguration.getExportedMetrics()) {
        if (Metrics.JOB_EXECUTOR_THREADS_METRICS.contains(metricName)) {
          if (jobExecutorThreadMetrics == null) {
            // lazy init of job executor metrics
            jobExecutorThreadMetrics = engineConfiguration.getJobExecutor().getThreadMetrics();
          }
          metricsSamples.add(new GaugeMetricSampleFamily(metricName, labelNames).addValue(labelValues, getJobExecutorMetric(metricName, jobExecutorThreadMetrics)));
        } else {
          long value = managementService.createMetricsQuery()
              .name(metricName)
              .reporter(reporterId)
              .sum();
          metricsSamples.add(new CounterMetricSampleFamily(metricName, labelNames).addValue(labelValues, value));
        }
      }
      return metricsSamples;
    }
    return Collections.emptyList();
  }

  private long getJobExecutorMetric(String metricName, JobExecutorThreadMetrics jobExecutorThreadMetrics) {
    switch (metricName) {
    case Metrics.JOB_EXECUTOR_THREADS_ACTIVE:
      return jobExecutorThreadMetrics.getThreadsActive();
    case Metrics.JOB_EXECUTOR_THREADS_BLOCKED:
      return jobExecutorThreadMetrics.getThreadsBlocked();
    case Metrics.JOB_EXECUTOR_THREADS_IDLE:
      return jobExecutorThreadMetrics.getThreadsIdle();
    case Metrics.JOB_EXECUTOR_THREADS_QUEUE:
      return jobExecutorThreadMetrics.getQueueSize();
    default:
      return 0;
    }
  }

}
