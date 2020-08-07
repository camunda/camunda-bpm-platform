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
package org.camunda.bpm.container.impl.ejb.plugin;

import org.camunda.bpm.container.impl.ejb.EjbBpmPlatformBootstrap;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;
import org.camunda.connect.Connectors;

public class EjbConnectProcessEnginePlugin extends AbstractProcessEnginePlugin {

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // use classloader which loaded the plugin
    ClassLoader classloader = ClassLoaderUtil.getClassloader(EjbBpmPlatformBootstrap.class);
    Connectors.loadConnectors(classloader);

    processEngineConfiguration.setTelemetryHttpConnector(Connectors.getConnector(Connectors.HTTP_CONNECTOR_ID));
  }
}
