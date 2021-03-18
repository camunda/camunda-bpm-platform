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

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.IsTelemetryEnabledCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.metrics.Meter;
import org.camunda.bpm.engine.impl.metrics.MetricsRegistry;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.telemetry.CommandCounter;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.engine.impl.telemetry.dto.Command;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Metric;
import org.camunda.bpm.engine.impl.telemetry.dto.Product;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.impl.util.TelemetryUtil;
import org.camunda.connect.spi.CloseableConnectorResponse;
import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorRequest;

public class TelemetrySendingTask extends TimerTask {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;
  protected static final Set<String> METRICS_TO_REPORT = new HashSet<>();
  protected static final String TELEMETRY_INIT_MESSAGE_SENT_NAME = "camunda.telemetry.initial.message.sent";
  protected static final String UUID4_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}";

  static {
    METRICS_TO_REPORT.add(ROOT_PROCESS_INSTANCE_START);
    METRICS_TO_REPORT.add(EXECUTED_DECISION_INSTANCES);
    METRICS_TO_REPORT.add(EXECUTED_DECISION_ELEMENTS);
    METRICS_TO_REPORT.add(ACTIVTY_INSTANCE_START);
  }

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected Data staticData;
  protected Connector<? extends ConnectorRequest<?>> httpConnector;
  protected int telemetryRequestRetries;
  protected TelemetryRegistry telemetryRegistry;
  protected MetricsRegistry metricsRegistry;
  protected int telemetryRequestTimeout;
  protected boolean sendInitialMessage;

  public TelemetrySendingTask(CommandExecutor commandExecutor,
                              String telemetryEndpoint,
                              int telemetryRequestRetries,
                              Data data,
                              Connector<? extends ConnectorRequest<?>> httpConnector,
                              TelemetryRegistry telemetryRegistry,
                              MetricsRegistry metricsRegistry,
                              int telemetryRequestTimeout,
                              boolean sendInitialMessage) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.telemetryRequestRetries = telemetryRequestRetries;
    this.staticData = data;
    this.httpConnector = httpConnector;
    this.telemetryRegistry = telemetryRegistry;
    this.metricsRegistry = metricsRegistry;
    this.telemetryRequestTimeout = telemetryRequestTimeout;
    this.sendInitialMessage = sendInitialMessage;
  }

  @Override
  public void run() {
    LOG.startTelemetrySendingTask();

    if (sendInitialMessage) {
      sendInitialMessage();
    }

    if (!isTelemetryEnabled()) {
      LOG.telemetryDisabled();
      updateTelemetryFlag(false);
      return;
    }

    updateTelemetryFlag(true);

    performDataSend(false, () -> {
      updateStaticData();
      Internals dynamicData = resolveDynamicData();
      Data mergedData = new Data(staticData);
      mergedData.mergeInternals(dynamicData);

      try {
        sendData(mergedData, false);
      } catch (Exception e) {
        // so that we send it again the next time
        restoreDynamicData(dynamicData);
        throw e;
      }
    });
  }

  protected void sendInitialMessage() {
    try {
      commandExecutor.execute(new SendInitialMsgCmd());
    } catch (ProcessEngineException pex) {
      // the property might have been inserted already by another cluster node after we checked it, ignore that
      if (!ExceptionUtil.checkConstraintViolationException(pex)) {
        LOG.exceptionWhileSendingTelemetryData(pex, true);
      }
    } catch (Exception e) {
      LOG.exceptionWhileSendingTelemetryData(e, true);
    }
  }

  protected void sendInitialMessage(CommandContext commandContext) {
    /*
     * check on init message property to minimize the risk of sending the
     * message twice in case another node in the cluster toggled the value
     * and successfully sent the message already - it is not 100% safe but
     * good enough as sending the message twice is still OK
     */
    if (null == commandContext.getPropertyManager().findPropertyById(TELEMETRY_INIT_MESSAGE_SENT_NAME)) {
      // message has not been sent yet
      performDataSend(true, () -> {
        Data initData = new Data(staticData.getInstallation(), new Product(staticData.getProduct()));
        Internals internals = new Internals();
        internals.setTelemetryEnabled(new IsTelemetryEnabledCmd().execute(commandContext));
        initData.getProduct().setInternals(internals);

        sendData(initData, true);
        sendInitialMessage = false;
        commandContext.getPropertyManager().insert(new PropertyEntity(TELEMETRY_INIT_MESSAGE_SENT_NAME, "true"));
      });
    } else {
      // message has already been sent by another node
      sendInitialMessage = false;
    }
  }

  protected void updateStaticData() {
    Internals internals = staticData.getProduct().getInternals();

    if (internals.getApplicationServer() == null) {
      ApplicationServer applicationServer = telemetryRegistry.getApplicationServer();
      internals.setApplicationServer(applicationServer);
    }

    if (internals.getTelemetryEnabled() == null) {
      internals.setTelemetryEnabled(true);// this can only be true, otherwise we would not collect data to send
    }

    // license key and Webapps data is fed from the outside to the registry but needs to be constantly updated
    internals.setLicenseKey(telemetryRegistry.getLicenseKey());
    internals.setWebapps(telemetryRegistry.getWebapps());
  }

  protected boolean isTelemetryEnabled() {
    Boolean telemetryEnabled = commandExecutor.execute(new IsTelemetryEnabledCmd());
    return telemetryEnabled != null && telemetryEnabled.booleanValue();
  }

  protected void sendData(Data dataToSend, boolean isInitialMessage) {

      String telemetryData = JsonUtil.asString(dataToSend);
      Map<String, Object> requestParams = assembleRequestParameters(METHOD_NAME_POST,
          telemetryEndpoint,
          MediaType.APPLICATION_JSON,
          telemetryData);
      requestParams = addRequestTimeoutConfiguration(requestParams, telemetryRequestTimeout);

      ConnectorRequest<?> request = httpConnector.createRequest();
      request.setRequestParameters(requestParams);

      LOG.sendingTelemetryData(telemetryData, isInitialMessage);
      CloseableConnectorResponse response = (CloseableConnectorResponse) request.execute();

      if (response == null) {
        LOG.unexpectedResponseWhileSendingTelemetryData(isInitialMessage);
      } else {
        int responseCode = (int) response.getResponseParameter(PARAM_NAME_RESPONSE_STATUS_CODE);

        if (isSuccessStatusCode(responseCode)) {
          if (responseCode != HttpURLConnection.HTTP_ACCEPTED) {
            LOG.unexpectedResponseSuccessCode(responseCode, isInitialMessage);
          }

          LOG.telemetrySentSuccessfully(isInitialMessage);

        } else {
          throw LOG.unexpectedResponseWhileSendingTelemetryData(responseCode, isInitialMessage);
        }
      }
  }

  /**
   * @return true if status code is 2xx
   */
  protected boolean isSuccessStatusCode(int statusCode) {
    return (statusCode / 100) == 2;
  }

  protected void clearDynamicData() {
    Internals internals = staticData.getProduct().getInternals();

    internals.setApplicationServer(null);
    internals.setCommands(null);
    internals.setMetrics(null);
    internals.setLicenseKey(null);
  }

  protected void restoreDynamicData(Internals internals) {
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

  protected Internals resolveDynamicData() {
    Internals result = new Internals();

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
    Map<String, CommandCounter> originalCounts = telemetryRegistry.getCommands();

    synchronized (originalCounts) {

      for (Map.Entry<String, CommandCounter> counter : originalCounts.entrySet()) {
        long occurrences = counter.getValue().getAndClear();
        commandsToReport.put(counter.getKey(), new Command(occurrences));
      }
    }

    return commandsToReport;
  }

  protected Map<String, Metric> calculateMetrics() {

    Map<String, Metric> metrics = new HashMap<>();

    if (metricsRegistry != null) {
      Map<String, Meter> telemetryMeters = metricsRegistry.getTelemetryMeters();

      for (String metricToReport : METRICS_TO_REPORT) {
        long value = telemetryMeters.get(metricToReport).getAndClear();
        metrics.put(metricToReport, new Metric(value));
      }
    }

    return metrics;
  }

  protected void updateTelemetryFlag(boolean enabled) {
    TelemetryUtil.updateCollectingTelemetryDataEnabled(telemetryRegistry, metricsRegistry, enabled);
  }

  protected class SendInitialMsgCmd implements org.camunda.bpm.engine.impl.interceptor.Command<Void> {
    @Override
    public Void execute(CommandContext commandContext) {
      sendInitialMessage(commandContext);
      return null;
    }
  }

  protected void performDataSend(Boolean isInitialMessage, Runnable runnable) {
    if (validateData(staticData)) {
      int triesLeft = telemetryRequestRetries + 1;
      boolean requestSuccessful = false;
      do {
        try {
          triesLeft--;

          runnable.run();

          requestSuccessful = true;
        } catch (Exception e) {
          LOG.exceptionWhileSendingTelemetryData(e, isInitialMessage);
        }
      } while (!requestSuccessful && triesLeft > 0);
    } else {
      LOG.sendingTelemetryDataFails(staticData);
    }
  }

  protected Boolean validateData(Data dataToSend) {
    // validate product data
    Product product = dataToSend.getProduct();
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
