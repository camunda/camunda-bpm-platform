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
package org.camunda.bpm.engine.impl.cmd;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.telemetry.CommandCounter;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.CommandImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.InternalsImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.MetricImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetrySendingTask;
import org.camunda.bpm.engine.telemetry.dto.Metric;

public class GetTelemetryDataCmd implements Command<TelemetryDataImpl> {

  ProcessEngineConfigurationImpl configuration;
  TelemetryRegistry telemetryRegistry;
  MetricsRegistry metricsRegistry;

  @Override
  public TelemetryDataImpl execute(CommandContext commandContext) {
    configuration = commandContext.getProcessEngineConfiguration();
    telemetryRegistry = configuration.getTelemetryRegistry();
    metricsRegistry = configuration.getMetricsRegistry();

    TelemetryDataImpl telemetryData = commandContext.getProcessEngineConfiguration().getTelemetryData();
    InternalsImpl internalsDynamicData = new InternalsImpl();
    internalsDynamicData.setCommands(getCommandCounts());
    internalsDynamicData.setMetrics(getMetrics());
    telemetryData.getProduct().getInternals().mergeDynamicData(internalsDynamicData);

    return telemetryData;
  }

  protected Map<String, org.camunda.bpm.engine.telemetry.dto.Command> getCommandCounts() {
    Map<String, org.camunda.bpm.engine.telemetry.dto.Command> commandsToReport = new HashMap<>();
    Map<String, CommandCounter> originalCounts = telemetryRegistry.getCommands();

    synchronized (originalCounts) {
      for (Map.Entry<String, CommandCounter> counter : originalCounts.entrySet()) {
        long occurrences = counter.getValue().get();
        commandsToReport.put(counter.getKey(), new CommandImpl(occurrences));
      }
    }
    return commandsToReport;
  }

  protected Map<String, Metric> getMetrics() {
    Map<String, Metric> metrics = new HashMap<>();
    if (metricsRegistry != null) {
      Map<String, Meter> telemetryMeters = metricsRegistry.getTelemetryMeters();

      for (String metricToReport : TelemetrySendingTask.METRICS_TO_REPORT) {
        long value = telemetryMeters.get(metricToReport).getAndClear();
        metrics.put(metricToReport, new MetricImpl(value));
      }
    }
    return metrics;
  }
}
