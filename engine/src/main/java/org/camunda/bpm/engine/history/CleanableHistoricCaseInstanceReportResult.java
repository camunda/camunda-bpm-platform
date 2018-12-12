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
 * This interface defines the result of Historic finished case instance report.
 *
 */
public interface CleanableHistoricCaseInstanceReportResult {

  /**
   * Returns the case definition id for the selected definition.
   */
  String getCaseDefinitionId();

  /**
   * Returns the case definition key for the selected definition.
   */
  String getCaseDefinitionKey();

  /**
   * Returns the case definition name for the selected definition.
   */
  String getCaseDefinitionName();

  /**
   * Returns the case definition version for the selected definition.
   */
  int getCaseDefinitionVersion();

  /**
   * Returns the history time to live for the selected definition.
   */
  Integer getHistoryTimeToLive();

  /**
   * Returns the amount of finished historic case instances.
   */
  long getFinishedCaseInstanceCount();

  /**
   * Returns the amount of cleanable historic case instances.
   */
  long getCleanableCaseInstanceCount();

  /**
   *
   * Returns the tenant id of the current case instances.
   */
  String getTenantId();
}
