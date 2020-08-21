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
import static org.camunda.bpm.engine.impl.util.ConnectUtil.assembleRequestParameters;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.telemetry.CommandCounter;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServer;
import org.camunda.bpm.engine.impl.telemetry.dto.Command;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.connect.spi.CloseableConnectorResponse;
import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorRequest;

public class TelemetrySendingTask extends TimerTask {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected Data data;
  protected Connector<? extends ConnectorRequest<?>> httpConnector;

  public TelemetrySendingTask(CommandExecutor commandExecutor,
                              String telemetryEndpoint,
                              Data data,
                              Connector<? extends ConnectorRequest<?>> httpConnector) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.data = data;
    this.httpConnector = httpConnector;
  }

  @Override
  public void run() {
    LOG.startTelemetrySendingTask();
    try {
      sendData();
    } catch (Exception e) {
      LOG.exceptionWhileSendingTelemetryData(e);
    }
  }

  protected void sendData() {
    commandExecutor.execute(commandContext -> {
      // send data only if telemetry is enabled
      ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();
      if (processEngineConfiguration.getManagementService().isTelemetryEnabled()) {
        try {
          resolveDataFromRegistry(processEngineConfiguration);

          String telemetryData = JsonUtil.asString(data);
          Map<String, Object> requestParams = assembleRequestParameters(METHOD_NAME_POST,
                                                                        telemetryEndpoint,
                                                                        MediaType.APPLICATION_JSON,
                                                                        telemetryData);
          ConnectorRequest<?> request = httpConnector.createRequest();
          request.setRequestParameters(requestParams);
          CloseableConnectorResponse response = (CloseableConnectorResponse) request.execute();

          if (response == null) {
            LOG.unexpectedResponseWhileSendingTelemetryData();
          } else {
            int responseCode = (int) response.getResponseParameter(PARAM_NAME_RESPONSE_STATUS_CODE);
            if (responseCode != HttpURLConnection.HTTP_ACCEPTED) {
              LOG.unexpectedResponseWhileSendingTelemetryData(responseCode);
            } else {
              LOG.telemetryDataSent(telemetryData);
            }
          }
        } catch (Exception e) {
          LOG.exceptionWhileSendingTelemetryData(e);
        }
      } else {
        LOG.telemetryDisabled();
      }
      return null;
    });
  }

  protected void resolveDataFromRegistry(ProcessEngineConfigurationImpl processEngineConfiguration) {
    Internals internals = data.getProduct().getInternals();
    ApplicationServer applicationServer = processEngineConfiguration.getTelemetryRegistry().getApplicationServer();
    if (internals.getApplicationServer() == null && applicationServer != null) {
      internals.setApplicationServer(applicationServer);
    }

    Map<String, Command> commands = fetchAndResetCommandCounts(processEngineConfiguration);
    internals.setCommands(commands);
  }

  protected Map<String, Command> fetchAndResetCommandCounts(ProcessEngineConfigurationImpl processEngineConfiguration) {
    Map<String, Command> commandsToReport = new HashMap<>();
    Map<String, CommandCounter> originalCounts = processEngineConfiguration.getTelemetryRegistry().getCommands();
    Map<String, CommandCounter> copiedCounts;
    synchronized (originalCounts) {
      // make a copy and empty the origin
      copiedCounts = new HashMap<>(originalCounts);
      originalCounts.clear();
    }

    copiedCounts.forEach((k, v) -> commandsToReport.put(k, new Command(v.get())));

    return commandsToReport;
  }

}
