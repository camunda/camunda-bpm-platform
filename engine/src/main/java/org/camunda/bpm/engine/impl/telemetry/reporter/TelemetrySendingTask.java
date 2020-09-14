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

import static org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry.UNIQUE_TASK_WORKERS;
import static org.camunda.bpm.engine.impl.util.ConnectUtil.METHOD_NAME_POST;
import static org.camunda.bpm.engine.impl.util.ConnectUtil.PARAM_NAME_RESPONSE_STATUS_CODE;
import static org.camunda.bpm.engine.impl.util.ConnectUtil.assembleRequestParameters;
import static org.camunda.bpm.engine.management.Metrics.ACTIVTY_INSTANCE_START;
import static org.camunda.bpm.engine.management.Metrics.EXECUTED_DECISION_INSTANCES;
import static org.camunda.bpm.engine.management.Metrics.ROOT_PROCESS_INSTANCE_START;

import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.IsTelemetryEnabledCmd;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.telemetry.CommandCounter;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.engine.impl.telemetry.dto.Command;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Metric;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.connect.spi.CloseableConnectorResponse;
import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorRequest;

public class TelemetrySendingTask extends TimerTask {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected Data staticData;
  protected Connector<? extends ConnectorRequest<?>> httpConnector;
  protected int telemetryRequestRetries;
  protected TelemetryRegistry telemetryRegistry;

  public TelemetrySendingTask(CommandExecutor commandExecutor,
                              String telemetryEndpoint,
                              int telemetryRequestRetries,
                              Data data,
                              Connector<? extends ConnectorRequest<?>> httpConnector,
                              TelemetryRegistry telemetryRegistry) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.telemetryRequestRetries = telemetryRequestRetries;
    this.staticData = data;
    this.httpConnector = httpConnector;
    this.telemetryRegistry = telemetryRegistry;
  }

  @Override
  public void run() {
    LOG.startTelemetrySendingTask();

    if (!isTelemetryEnabled()) {
      LOG.telemetryDisabled();
      return;
    }

    int triesLeft = telemetryRequestRetries + 1;
    boolean requestSuccessful = false;
    do {
      try {
        triesLeft--;

        updateStaticData();
        Internals dynamicData = resolveDynamicData();
        Data mergedData = new Data(staticData);
        mergedData.mergeInternals(dynamicData);

        try {
          sendData(mergedData);
        } catch (Exception e) {
          // so that we send it again the next time
          restoreDynamicData(dynamicData);
          throw e;
        }

        // reset report time
        telemetryRegistry.setStartReportTime(ClockUtil.getCurrentTime());

        requestSuccessful = true;
      } catch (Exception e) {
        LOG.exceptionWhileSendingTelemetryData(e);
      }
    } while (!requestSuccessful && triesLeft > 0);
  }

  protected void updateStaticData() {
    Internals internals = staticData.getProduct().getInternals();

    if (internals.getApplicationServer() == null) {
      ApplicationServer applicationServer = telemetryRegistry.getApplicationServer();
      internals.setApplicationServer(applicationServer);
    }
  }

  protected boolean isTelemetryEnabled() {
    return commandExecutor.execute(new IsTelemetryEnabledCmd());
  }

  protected void sendData(Data dataToSend) {

      String telemetryData = JsonUtil.asString(dataToSend);
      Map<String, Object> requestParams = assembleRequestParameters(METHOD_NAME_POST,
          telemetryEndpoint,
          MediaType.APPLICATION_JSON,
          telemetryData);

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

  protected void clearDynamicData() {
    Internals internals = staticData.getProduct().getInternals();

    internals.setApplicationServer(null);
    internals.setCommands(null);
    internals.setMetrics(null);
  }

  protected void restoreDynamicData(Internals internals) {
    Map<String, Command> commands = internals.getCommands();

    for (Map.Entry<String, Command> entry : commands.entrySet()) {
      telemetryRegistry.markOccurrence(entry.getKey(), entry.getValue().getCount());
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

    Date startReportTime = telemetryRegistry.getStartReportTime();
    Date currentTime = ClockUtil.getCurrentTime();

    return commandExecutor.execute(c -> {
      ManagementService managementService = c.getProcessEngineConfiguration().getManagementService();

      Map<String, Metric> metrics = new HashMap<>();

      long sum = calculateMetricCount(managementService, startReportTime, currentTime, ROOT_PROCESS_INSTANCE_START);
      metrics.put(ROOT_PROCESS_INSTANCE_START, new Metric(sum));

      sum = calculateMetricCount(managementService, startReportTime, currentTime, EXECUTED_DECISION_INSTANCES);
      metrics.put(EXECUTED_DECISION_INSTANCES, new Metric(sum));

      sum = calculateMetricCount(managementService, startReportTime, currentTime, ACTIVTY_INSTANCE_START);
      metrics.put(ACTIVTY_INSTANCE_START, new Metric(sum));

      sum = calculateUniqueUserCount(c, c.getProcessEngineConfiguration().getHistoryLevel(), startReportTime, currentTime);
      metrics.put(UNIQUE_TASK_WORKERS, new Metric(sum));

      return metrics;
    });
  }

  protected long calculateMetricCount(ManagementService managementService,
                                      Date startReportTime,
                                      Date currentTime,
                                      String metricName) {
    return managementService.createMetricsQuery()
        .name(metricName)
        .startDate(startReportTime)
        .endDate(currentTime)
        .sum();
  }

  protected long calculateUniqueUserCount(CommandContext commandContext,
                                          HistoryLevel historyLevel,
                                          Date startReportTime,
                                          Date currentTime) {
    if (historyLevel.equals(HistoryLevel.HISTORY_LEVEL_NONE)) {
      return 0;
    } else {
      Date previousDay = previousDay(currentTime);
      long workerCount = commandContext.getHistoricTaskInstanceManager()
          .findUniqueTaskWorkerCount(previousDay, currentTime);
      return workerCount;
    }
  }

  protected Date previousDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DAY_OF_YEAR, -1);
    Date result = calendar.getTime();
    return result;
  }

}
