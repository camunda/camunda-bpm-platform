/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.history;

/**
 * This interface defines the result of Cleanable historic process instance report.
 *
 */
public interface CleanableHistoricProcessInstanceReportResult {

  /**
   * Returns the process definition id for the selected definition.
   */
  String getProcessDefinitionId();

  /**
   * Returns the process definition key for the selected definition.
   */
  String getProcessDefinitionKey();

  /**
   * Returns the process definition name for the selected definition.
   */
  String getProcessDefinitionName();

  /**
   * Returns the process definition version for the selected definition.
   */
  int getProcessDefinitionVersion();

  /**
   * Returns the history time to live for the selected definition.
   */
  Integer getHistoryTimeToLive();

  /**
   * Returns the amount of finished historic process instances.
   */
  long getFinishedProcessInstanceCount();

  /**
   * Returns the amount of cleanable historic process instances.
   */
  long getCleanableProcessInstanceCount();

  /**
   *
   * Returns the tenant id of the current process instances.
   */
  String getTenantId();
}
