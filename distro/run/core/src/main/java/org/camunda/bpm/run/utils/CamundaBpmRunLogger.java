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
package org.camunda.bpm.run.utils;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.commons.logging.BaseLogger;

public class CamundaBpmRunLogger extends BaseLogger {

  public static final String PROJECT_CODE = "RUN";
  public static final String PROJECT_ID = "CR";
  public static final String PACKAGE = "org.camunda.bpm.run";

  public static final CamundaBpmRunLogger LOG = createLogger(CamundaBpmRunLogger.class, PROJECT_CODE, PACKAGE, PROJECT_ID);

  public void processEnginePluginRegistered(String pluginClass) {
      logInfo("001",
              "The process engine plugin '{}' was registered with the " +
                  "Camunda Run process engine.",
              pluginClass);
  }

  public ProcessEngineException failedProcessEnginePluginInstantiation(String pluginClass, Exception e) {
    return new ProcessEngineException(
        exceptionMessage("002",
                         "Unable to register the process engine plugin '{}'. " +
                             "Please ensure that the correct plugin class is configured in your " +
                             "YAML configuration file, and that the class is present on the " +
                             "classpath. More details: {}",
                         pluginClass, e.getMessage(), e));
  }

  public ProcessEngineException pluginPropertyNotFound(String pluginName, String propertyName, Exception e) {
    return new ProcessEngineException(
        exceptionMessage("003",
                         "Please check the configuration options for plugin '{}'. " +
                             "Some configuration parameters could not be found. More details: {}",
                         pluginName, e.getMessage(), e));
  }

}