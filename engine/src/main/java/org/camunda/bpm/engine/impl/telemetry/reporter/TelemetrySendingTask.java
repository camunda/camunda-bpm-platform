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

import java.util.TimerTask;

import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.connect.httpclient.HttpConnector;
import org.camunda.connect.httpclient.HttpResponse;

public class TelemetrySendingTask extends TimerTask {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected Data data;
  protected HttpConnector http;

  public TelemetrySendingTask(CommandExecutor commandExecutor,
                              String telemetryEndpoint,
                              Data data,
                              HttpConnector http) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.data = data;
    this.http = http;
  }

  @Override
  public void run() {
    LOG.startTelemetrySendingTask();
    try {
      sendData();
    } catch (Exception e) {
      LOG.exceptionWhileSendingTelemetryData(e.getMessage());
    }
  }

  protected void sendData() {
    commandExecutor.execute(commandContext -> {
      // send data only if telemetry is enabled
      if (commandContext.getProcessEngineConfiguration().getManagementService().isTelemetryEnabled()) {
        try {
          String telemetryData = JsonUtil.asString(data);
          HttpResponse response = http.createRequest()
              .url(telemetryEndpoint)
              .post()
              .contentType(MediaType.APPLICATION_JSON)
              .payload(telemetryData)
              .execute();

          if (response == null || response.getStatusCode() != 202) {
            LOG.unexpectedResponseWhileSendingTelemetryData();
          } else {
            LOG.telemetryDataSent(telemetryData);
          }
        } catch (Exception e) {
          LOG.exceptionWhileSendingTelemetryData(e.getMessage());
        }
      } else {
        LOG.telemetryDisabled();
      }
      return null;
    });
  }

}
