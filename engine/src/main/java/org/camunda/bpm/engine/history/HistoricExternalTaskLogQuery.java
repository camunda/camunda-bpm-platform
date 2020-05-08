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
package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.Query;

public interface HistoricExternalTaskLogQuery extends Query<HistoricExternalTaskLogQuery, HistoricExternalTaskLog> {

  /** Only select historic external task log entries with the id. */
  HistoricExternalTaskLogQuery logId(String historicExternalTaskLogId);

  /** Only select historic external task log entries with the given external task id. */
  HistoricExternalTaskLogQuery externalTaskId(String taskId);

  /** Only select historic external task log entries with the given topic name. */
  HistoricExternalTaskLogQuery topicName(String topicName);

  /** Only select historic external task log entries with the given worker id. */
  HistoricExternalTaskLogQuery workerId(String workerId);

  /** Only select historic external task log entries with the given error message. */
  HistoricExternalTaskLogQuery errorMessage(String errorMessage);

  /** Only select historic external task log entries which are associated with one of the given activity ids. **/
  HistoricExternalTaskLogQuery activityIdIn(String... activityIds);

  /** Only select historic external task log entries which are associated with one of the given activity instance ids. **/
  HistoricExternalTaskLogQuery activityInstanceIdIn(String... activityInstanceIds);

  /** Only select historic external task log entries which are associated with one of the given execution ids. **/
  HistoricExternalTaskLogQuery executionIdIn(String... executionIds);

  /** Only select historic external task log entries with the process instance id. */
  HistoricExternalTaskLogQuery processInstanceId(String processInstanceId);

  /** Only select historic external task log entries with the process definition id. */
  HistoricExternalTaskLogQuery processDefinitionId(String processDefinitionId);

  /** Only select historic external task log entries with the process instance key. */
  HistoricExternalTaskLogQuery processDefinitionKey(String processDefinitionKey);

  /** Only select historic external task log entries that belong to one of the given tenant ids. */
  HistoricExternalTaskLogQuery tenantIdIn(String... tenantIds);

  /** Only selects historic external task log entries that have no tenant id. */
  HistoricExternalTaskLogQuery withoutTenantId();

  /**
   * Only select log entries where the external task had a priority higher than or
   * equal to the given priority.
   */
  HistoricExternalTaskLogQuery priorityHigherThanOrEquals(long priority);

  /**
   * Only select log entries where the external task had a priority lower than or
   * equal to the given priority.
   */
  HistoricExternalTaskLogQuery priorityLowerThanOrEquals(long priority);

  /** Only select created historic external task log entries. */
  HistoricExternalTaskLogQuery creationLog();

  /** Only select failed historic external task log entries. */
  HistoricExternalTaskLogQuery failureLog();

  /** Only select successful historic external task log entries. */
  HistoricExternalTaskLogQuery successLog();

  /** Only select deleted historic external task log entries. */
  HistoricExternalTaskLogQuery deletionLog();


  /** Order by timestamp (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByTimestamp();

  /** Order by external task id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByExternalTaskId();

  /** Order by external task retries (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByRetries();

  /**
   * Order by external task priority (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricExternalTaskLogQuery orderByPriority();

  /** Order by topic name (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByTopicName();

  /** Order by worker id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByWorkerId();

  /** Order by activity id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByActivityId();

  /** Order by activity instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByActivityInstanceId();

  /** Order by execution id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByExecutionId();

  /** Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByProcessInstanceId();

  /** Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByProcessDefinitionId();

  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricExternalTaskLogQuery orderByProcessDefinitionKey();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of external task log entries without tenant id is database-specific.
   */
  HistoricExternalTaskLogQuery orderByTenantId();
}
