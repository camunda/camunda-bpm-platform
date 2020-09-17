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
package org.camunda.bpm.container.impl.ejb.deployment;

import java.util.List;

import org.camunda.bpm.container.impl.deployment.StartProcessEngineStep;
import org.camunda.bpm.container.impl.ejb.plugin.EjbConnectProcessEnginePlugin;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.telemetry.CamundaIntegration;
import org.camunda.bpm.engine.impl.telemetry.TelemetryRegistry;

/**
 * Adds an additional configuration before the engine is built
 */
public class EjbStartProcessEngineStep extends StartProcessEngineStep {

  protected static final String CONNECT_PROCESS_ENGINE_PLUGIN = "org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin";

  public EjbStartProcessEngineStep(ProcessEngineXml processEngineXml) {
    super(processEngineXml);
  }

  @Override
  protected void addAdditionalPlugins(ProcessEngineConfigurationImpl configuration) {
    boolean isConnectPluginAdded = false;
    List<ProcessEnginePlugin> processEnginePlugins = configuration.getProcessEnginePlugins();
    for (ProcessEnginePlugin processEnginePlugin : processEnginePlugins) {
      if (processEnginePlugin.getClass().getCanonicalName().equals(CONNECT_PROCESS_ENGINE_PLUGIN)) {
        isConnectPluginAdded = true;
        break;
      }
    }
    if (!isConnectPluginAdded) {
      // add the plugin only if the connect plugin has not been added so far
      // the plugin will initialize the connectors with the current class loader
      processEnginePlugins.add(new EjbConnectProcessEnginePlugin());
    }
  }

  protected void additionalConfiguration(ProcessEngineConfigurationImpl configuration) {
    TelemetryRegistry telemetryRegistry = new TelemetryRegistry();
    telemetryRegistry.setCamundaIntegration(CamundaIntegration.CAMUNDA_EJB_SERVICE);
    configuration.setTelemetryRegistry(telemetryRegistry);
  }
}
