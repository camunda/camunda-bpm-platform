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
package org.camunda.bpm.engine.impl.diagnostics;

import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.ROOT_PROCESS_INSTANCE_START;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.metrics.util.MetricsUtil;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServerImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.CommandImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.InternalsImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.MetricImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.telemetry.Command;
import org.camunda.bpm.engine.telemetry.Metric;

public class DiagnosticsCollector {

  protected static final Set<String> METRICS_TO_REPORT = Set.of(ROOT_PROCESS_INSTANCE_START,
                                                                EXECUTED_DECISION_INSTANCES,
                                                                EXECUTED_DECISION_ELEMENTS,
                                                                ACTIVTY_INSTANCE_START);

  protected TelemetryDataImpl staticData;
  protected DiagnosticsRegistry diagnosticsRegistry;
  protected MetricsRegistry metricsRegistry;

  public DiagnosticsCollector(TelemetryDataImpl data,
                              DiagnosticsRegistry diagnosticsRegistry,
                              MetricsRegistry metricsRegistry) {
    this.staticData = data;
    this.diagnosticsRegistry = diagnosticsRegistry;
    this.metricsRegistry = metricsRegistry;
  }

  public TelemetryDataImpl updateAndFetchData() {
    updateStaticData();
    InternalsImpl dynamicData = resolveDynamicData();
    TelemetryDataImpl mergedData = new TelemetryDataImpl(staticData);
    mergedData.mergeInternals(dynamicData);

    return mergedData;
  }

  protected void updateStaticData() {
    InternalsImpl internals = staticData.getProduct().getInternals();

    if (internals.getApplicationServer() == null) {
      ApplicationServerImpl applicationServer = diagnosticsRegistry.getApplicationServer();
      internals.setApplicationServer(applicationServer);
    }

    // license key and Webapps data is fed from the outside to the registry but needs to be constantly updated
    internals.setLicenseKey(diagnosticsRegistry.getLicenseKey());
    internals.setWebapps(diagnosticsRegistry.getWebapps());
  }

  protected InternalsImpl resolveDynamicData() {
    InternalsImpl result = new InternalsImpl();

    Map<String, Metric> metrics = calculateMetrics();
    result.setMetrics(metrics);

    // command counts are modified after the metrics are retrieved, because
    // metric retrieval can fail and resetting the command count is a side effect
    // that we would otherwise have to undo
    Map<String, Command> commands = fetchAndResetCommandCounts();
    result.setCommands(commands);

    return result;
  }

  protected Map<String, Command> fetchAndResetCommandCounts() {
    Map<String, Command> commandsToReport = new HashMap<>();
    Map<String, CommandCounter> originalCounts = diagnosticsRegistry.getCommands();

    synchronized (originalCounts) {

      for (Map.Entry<String, CommandCounter> counter : originalCounts.entrySet()) {
        long occurrences = counter.getValue().get();
        commandsToReport.put(counter.getKey(), new CommandImpl(occurrences));
      }
    }

    return commandsToReport;
  }

  protected Map<String, Metric> calculateMetrics() {

    Map<String, Metric> metrics = new HashMap<>();

    if (metricsRegistry != null) {
      Map<String, Meter> telemetryMeters = metricsRegistry.getDiagnosticsMeters();

      for (String metricToReport : METRICS_TO_REPORT) {
        long value = telemetryMeters.get(metricToReport).get();

        // add public names
        metrics.put(MetricsUtil.resolvePublicName(metricToReport), new MetricImpl(value));
      }
    }

    return metrics;
  }

}
