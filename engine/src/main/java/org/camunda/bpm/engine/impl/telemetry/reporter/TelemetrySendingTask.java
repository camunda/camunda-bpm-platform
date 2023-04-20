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
package org.camunda.bpm.engine.impl.telemetry.reporter;

import static org.camunda.bpm.engine.impl.util.ConnectUtil.METHOD_NAME_POST;
import static org.camunda.bpm.engine.impl.util.ConnectUtil.PARAM_NAME_RESPONSE_STATUS_CODE;
import static org.camunda.bpm.engine.impl.util.ConnectUtil.addRequestTimeoutConfiguration;
import static org.camunda.bpm.engine.impl.util.ConnectUtil.assembleRequestParameters;
import static org.camunda.bpm.engine.impl.util.StringUtil.hasText;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_ELEMENTS;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.ROOT_PROCESS_INSTANCE_START;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.IsTelemetryEnabledCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.metrics.util.MetricsUtil;
import org.camunda.bpm.engine.impl.telemetry.CommandCounter;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServerImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.CommandImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.InternalsImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.MetricImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.ProductImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.TelemetryDataImpl;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.TelemetryUtil;
import org.camunda.bpm.engine.telemetry.Command;
import org.camunda.bpm.engine.telemetry.Metric;
import org.camunda.connect.spi.CloseableConnectorResponse;
import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorRequest;

public class TelemetrySendingTask extends TimerTask {

  protected static final Set<String> METRICS_TO_REPORT = new HashSet<>();
  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;
  protected static final String UUID4_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";

  static {
    METRICS_TO_REPORT.add(ROOT_PROCESS_INSTANCE_START);
    METRICS_TO_REPORT.add(EXECUTED_DECISION_INSTANCES);
    METRICS_TO_REPORT.add(EXECUTED_DECISION_ELEMENTS);
    METRICS_TO_REPORT.add(ACTIVTY_INSTANCE_START);
  }

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected TelemetryDataImpl staticData;
  protected Connector<? extends ConnectorRequest<?>> httpConnector;
  protected int telemetryRequestRetries;
  protected TelemetryRegistry telemetryRegistry;
  protected MetricsRegistry metricsRegistry;
  protected int telemetryRequestTimeout;

  public TelemetrySendingTask(CommandExecutor commandExecutor,
                              String telemetryEndpoint,
                              int telemetryRequestRetries,
                              TelemetryDataImpl data,
                              Connector<? extends ConnectorRequest<?>> httpConnector,
                              TelemetryRegistry telemetryRegistry,
                              MetricsRegistry metricsRegistry,
                              int telemetryRequestTimeout) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.telemetryRequestRetries = telemetryRequestRetries;
    this.staticData = data;
    this.httpConnector = httpConnector;
    this.telemetryRegistry = telemetryRegistry;
    this.metricsRegistry = metricsRegistry;
    this.telemetryRequestTimeout = telemetryRequestTimeout;
  }

  @Override
  public void run() {
    LOG.startTelemetrySendingTask();

    if (!isTelemetryEnabled()) {
      LOG.telemetryDisabled();
      return;
    }

    TelemetryUtil.toggleLocalTelemetry(true, telemetryRegistry, metricsRegistry);

    performDataSend(() -> updateAndSendData(true, true));
  }

  public TelemetryDataImpl updateAndSendData(boolean sendData, boolean addLegacyNames) {
    updateStaticData();
    InternalsImpl dynamicData = resolveDynamicData(sendData, addLegacyNames);
    TelemetryDataImpl mergedData = new TelemetryDataImpl(staticData);
    mergedData.mergeInternals(dynamicData);

    if(sendData) {
      try {
        sendData(mergedData);
        // reset data collection time frame on successful submit
        updateDataCollectionStartDate();
      } catch (Exception e) {
        // so that we send it again the next time
        restoreDynamicData(dynamicData);
        throw e;
      }
    }
    return mergedData;
  }

  protected void updateStaticData() {
    InternalsImpl internals = staticData.getProduct().getInternals();

    if (internals.getApplicationServer() == null) {
      ApplicationServerImpl applicationServer = telemetryRegistry.getApplicationServer();
      internals.setApplicationServer(applicationServer);
    }

    if (internals.isTelemetryEnabled() == null) {
      internals.setTelemetryEnabled(true);// this can only be true, otherwise we would not collect data to send
    }

    // license key and Webapps data is fed from the outside to the registry but needs to be constantly updated
    internals.setLicenseKey(telemetryRegistry.getLicenseKey());
    internals.setWebapps(telemetryRegistry.getWebapps());
  }

  public void updateDataCollectionStartDate() {
    staticData.getProduct().getInternals().setDataCollectionStartDate(ClockUtil.getCurrentTime());
  }

  protected boolean isTelemetryEnabled() {
    Boolean telemetryEnabled = commandExecutor.execute(new IsTelemetryEnabledCmd());
    return telemetryEnabled != null && telemetryEnabled.booleanValue();
  }

  protected void sendData(TelemetryDataImpl dataToSend) {

      String telemetryData = JsonUtil.asString(dataToSend);
      Map<String, Object> requestParams = assembleRequestParameters(METHOD_NAME_POST,
          telemetryEndpoint,
          "application/json",
          telemetryData);
      requestParams = addRequestTimeoutConfiguration(requestParams, telemetryRequestTimeout);

      ConnectorRequest<?> request = httpConnector.createRequest();
      request.setRequestParameters(requestParams);

      LOG.sendingTelemetryData(telemetryData);
      CloseableConnectorResponse response = (CloseableConnectorResponse) request.execute();

      if (response == null) {
        LOG.unexpectedResponseWhileSendingTelemetryData();
      } else {
        int responseCode = (int) response.getResponseParameter(PARAM_NAME_RESPONSE_STATUS_CODE);

        if (isSuccessStatusCode(responseCode)) {
          if (responseCode != HttpURLConnection.HTTP_ACCEPTED) {
            LOG.unexpectedResponseSuccessCode(responseCode);
          }

          LOG.telemetrySentSuccessfully();

        } else {
          throw LOG.unexpectedResponseWhileSendingTelemetryData(responseCode);
        }
      }
  }

  /**
   * @return true if status code is 2xx
   */
  protected boolean isSuccessStatusCode(int statusCode) {
    return (statusCode / 100) == 2;
  }

  protected void restoreDynamicData(InternalsImpl internals) {
    Map<String, Command> commands = internals.getCommands();

    for (Map.Entry<String, Command> entry : commands.entrySet()) {
      telemetryRegistry.markOccurrence(entry.getKey(), entry.getValue().getCount());
    }

    if (metricsRegistry != null) {
      Map<String, Metric> metrics = internals.getMetrics();

      for (String metricToReport : METRICS_TO_REPORT) {
        Metric metricValue = metrics.get(metricToReport);
        metricsRegistry.markTelemetryOccurrence(metricToReport, metricValue.getCount());
      }
    }
  }

  protected InternalsImpl resolveDynamicData(boolean reset, boolean addLegacyNames) {
    InternalsImpl result = new InternalsImpl();

    Map<String, Metric> metrics = calculateMetrics(reset, addLegacyNames);
    result.setMetrics(metrics);

    // command counts are modified after the metrics are retrieved, because
    // metric retrieval can fail and resetting the command count is a side effect
    // that we would otherwise have to undo
    Map<String, Command> commands = fetchAndResetCommandCounts(reset);
    result.setCommands(commands);

    return result;
  }

  protected Map<String, Command> fetchAndResetCommandCounts(boolean reset) {
    Map<String, Command> commandsToReport = new HashMap<>();
    Map<String, CommandCounter> originalCounts = telemetryRegistry.getCommands();

    synchronized (originalCounts) {

      for (Map.Entry<String, CommandCounter> counter : originalCounts.entrySet()) {
        long occurrences = counter.getValue().get(reset);
        commandsToReport.put(counter.getKey(), new CommandImpl(occurrences));
      }
    }

    return commandsToReport;
  }

  protected Map<String, Metric> calculateMetrics(boolean reset, boolean addLegacyNames) {

    Map<String, Metric> metrics = new HashMap<>();

    if (metricsRegistry != null) {
      Map<String, Meter> telemetryMeters = metricsRegistry.getTelemetryMeters();

      for (String metricToReport : METRICS_TO_REPORT) {
        long value = telemetryMeters.get(metricToReport).get(reset);

        if (addLegacyNames) {
          metrics.put(metricToReport, new MetricImpl(value));
        }

        // add public names
        metrics.put(MetricsUtil.resolvePublicName(metricToReport), new MetricImpl(value));
      }
    }

    return metrics;
  }

  protected void performDataSend(Runnable runnable) {
    if (validateData(staticData)) {
      int triesLeft = telemetryRequestRetries + 1;
      boolean requestSuccessful = false;
      do {
        try {
          triesLeft--;

          runnable.run();

          requestSuccessful = true;
        } catch (Exception e) {
          LOG.exceptionWhileSendingTelemetryData(e);
        }
      } while (!requestSuccessful && triesLeft > 0);
    } else {
      LOG.sendingTelemetryDataFails(staticData);
    }
  }

  protected Boolean validateData(TelemetryDataImpl dataToSend) {
    // validate product data
    ProductImpl product = dataToSend.getProduct();
    String installationId = dataToSend.getInstallation();
    String edition = product.getEdition();
    String version = product.getVersion();
    String name = product.getName();

    // ensure that data is not null or empty strings
    boolean validProductData = hasText(name) && hasText(version) && hasText(edition) && hasText(installationId);

    // validate installation id
    if (validProductData) {
      validProductData = validProductData && installationId.matches(UUID4_PATTERN);
    }

    return validProductData;
  }

}
