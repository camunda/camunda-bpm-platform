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

import java.nio.charset.StandardCharsets;
import java.util.TimerTask;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.telemetry.TelemetryLogger;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;

import com.google.gson.Gson;

public class TelemetrySendingTask extends TimerTask {

  protected static final TelemetryLogger LOG = ProcessEngineLogger.TELEMETRY_LOGGER;

  protected CommandExecutor commandExecutor;
  protected String telemetryEndpoint;
  protected Data data;
  protected HttpClient httpClient;

  public TelemetrySendingTask(CommandExecutor commandExecutor,
                              String telemetryEndpoint,
                              Data data,
                              HttpClient httpClient) {
    this.commandExecutor = commandExecutor;
    this.telemetryEndpoint = telemetryEndpoint;
    this.data = data;
    this.httpClient = httpClient;
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
    commandExecutor.execute(new Command<Void>() {
      @Override
      public Void execute(CommandContext commandContext) {
        // send data only if telemetry is enabled
        if (commandContext.getProcessEngineConfiguration().getManagementService().isTelemetryEnabled()) {
          try {
            HttpPost request = new HttpPost(telemetryEndpoint);
            String telemetryData = new Gson().toJson(data);
            StringEntity requestBody = new StringEntity(telemetryData, StandardCharsets.UTF_8);
            request.setHeader("content-type", MediaType.APPLICATION_JSON);
            request.setEntity(requestBody);
            HttpResponse response = httpClient.execute(request);

            if (response == null || HttpStatus.SC_ACCEPTED != response.getStatusLine().getStatusCode()) {
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
      }
    });
  }

}
