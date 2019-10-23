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
package org.camunda.commons.testing.util;

import org.camunda.commons.logging.BaseLogger;

public class ExampleProcessEngineLogger extends BaseLogger {
  public static final String PROJECT_CODE = "ENGINE";

  public static final ExampleProcessEngineLogger PERSISTENCE_LOGGER = BaseLogger.createLogger(
      ExampleProcessEngineLogger.class, PROJECT_CODE, "org.camunda.bpm.engine.persistence", "03");

  public static final ExampleProcessEngineLogger CONTAINER_INTEGRATION_LOGGER = BaseLogger.createLogger(
      ExampleProcessEngineLogger.class, PROJECT_CODE, "org.camunda.bpm.container", "08");

  public static final ExampleProcessEngineLogger JOB_EXECUTOR_LOGGER = BaseLogger.createLogger(
      ExampleProcessEngineLogger.class, PROJECT_CODE, "org.camunda.bpm.engine.jobexecutor", "14");

  public static final ExampleProcessEngineLogger PROCESS_APPLICATION_LOGGER = BaseLogger.createLogger(
      ExampleProcessEngineLogger.class, PROJECT_CODE, "org.camunda.bpm.application", "07");

  public void info() {
    logInfo("01", "This is an INFO log in component {}}!", this.componentId);
  }

  public void debug() {
    logDebug("02", "This is a DEBUG log in component {}}!", this.componentId);
  }

  public void warn() {
    logWarn("03", "This is a WARN log in component {}}!", this.componentId);
  }

  public void error() {
    logError("04", "This is an ERROR log in component {}}!", this.componentId);
  }
}