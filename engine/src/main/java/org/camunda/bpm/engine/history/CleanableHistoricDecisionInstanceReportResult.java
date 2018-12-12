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
 * This interface defines the result of Cleanable historic decision instance report.
 *
 */
public interface CleanableHistoricDecisionInstanceReportResult {

  /**
   * Returns the decision definition id for the selected definition.
   */
  String getDecisionDefinitionId();

  /**
   * Returns the decision definition key for the selected definition.
   */
  String getDecisionDefinitionKey();

  /**
   * Returns the decision definition name for the selected definition.
   */
  String getDecisionDefinitionName();

  /**
   * Returns the decision definition version for the selected definition.
   */
  int getDecisionDefinitionVersion();

  /**
   * Returns the history time to live for the selected definition.
   */
  Integer getHistoryTimeToLive();

  /**
   * Returns the amount of finished historic decision instances.
   */
  long getFinishedDecisionInstanceCount();

  /**
   * Returns the amount of cleanable historic decision instances.
   */
  long getCleanableDecisionInstanceCount();

  /**
   *
   * Returns the tenant id of the current decision instances.
   */
  String getTenantId();
}
