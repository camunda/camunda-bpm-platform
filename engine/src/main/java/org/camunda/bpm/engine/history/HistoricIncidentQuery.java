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

import java.util.Date;

/**
 * @author Roman Smirnov
 *
 */
public interface HistoricIncidentQuery extends Query<HistoricIncidentQuery, HistoricIncident> {

  /** Only select historic incidents which have the given id. **/
  HistoricIncidentQuery incidentId(String incidentId);

  /** Only select historic incidents which have the given incident type. **/
  HistoricIncidentQuery incidentType(String incidentType);

  /** Only select historic incidents which have the given incident message. **/
  HistoricIncidentQuery incidentMessage(String incidentMessage);

  /**
   * Only select historic incidents which incident message is like the given value
   *
   * @param incidentMessageLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  HistoricIncidentQuery incidentMessageLike(String incidentMessageLike);

  /** Only select historic incidents which have the given process definition id. **/
  HistoricIncidentQuery processDefinitionId(String processDefinitionId);

  /** Only select historic incidents which have the given processDefinitionKey. **/
  HistoricIncidentQuery processDefinitionKey(String processDefinitionKey);

  /** Only select historic incidents which have one of the given process definition keys. **/
  HistoricIncidentQuery processDefinitionKeyIn(String... processDefinitionKeys);

  /** Only select historic incidents which have the given process instance id. **/
  HistoricIncidentQuery processInstanceId(String processInstanceId);

  /** Only select historic incidents with the given id. **/
  HistoricIncidentQuery executionId(String executionId);

  /** Only select historic incidents which have a createTime date before the given date **/
  HistoricIncidentQuery createTimeBefore(Date createTimeBefore);

  /** Only select historic incidents which have a createTime date after the given date **/
  HistoricIncidentQuery createTimeAfter(Date createTimeAfter);

  /** Only select historic incidents which have an endTimeBefore date before the given date **/
  HistoricIncidentQuery endTimeBefore(Date endTimeBefore);

  /** Only select historic incidents which have an endTimeAfter date after the given date **/
  HistoricIncidentQuery endTimeAfter(Date endTimeAfter);

  /** Only select historic incidents which contain an activity with the given id. **/
  HistoricIncidentQuery activityId(String activityId);

  /** Only select historic incidents which were created due to a failure at an activity with the given id. **/
  HistoricIncidentQuery failedActivityId(String activityId);

  /** Only select historic incidents which contain the id of the cause incident. **/
  HistoricIncidentQuery causeIncidentId(String causeIncidentId);

  /** Only select historic incidents which contain the id of the root cause incident. **/
  HistoricIncidentQuery rootCauseIncidentId(String rootCauseIncidentId);

  /** Only select historic incidents that belong to one of the given tenant ids. */
  HistoricIncidentQuery tenantIdIn(String... tenantIds);

  /** Only selects historic incidents that have no tenant id. */
  HistoricIncidentQuery withoutTenantId();

  /** Only select incidents which contain the configuration. **/
  HistoricIncidentQuery configuration(String configuration);

  /** Only select incidents which contain the historyConfiguration. **/
  HistoricIncidentQuery historyConfiguration(String historyConfiguration);

  /** Only select incidents that belong to one of the given job definition ids. */
  HistoricIncidentQuery jobDefinitionIdIn(String... jobDefinitionIds);

  /** Only select historic incidents which are open. **/
  HistoricIncidentQuery open();

  /** Only select historic incidents which are resolved. **/
  HistoricIncidentQuery resolved();

  /** Only select historic incidents which are deleted. **/
  HistoricIncidentQuery deleted();

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByIncidentId();

  /** Order by message (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByIncidentMessage();

  /** Order by create time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByCreateTime();

  /** Order by end time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByEndTime();

  /** Order by incidentType (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByIncidentType();

  /** Order by executionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByExecutionId();

  /** Order by activityId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByActivityId();

  /** Order by processInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByProcessInstanceId();

  /** Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByProcessDefinitionId();

  /** Order by processDefinitionKey (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByProcessDefinitionKey();

  /** Order by causeIncidentId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByCauseIncidentId();

  /** Order by rootCauseIncidentId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByRootCauseIncidentId();

  /** Order by configuration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByConfiguration();

  /** Order by historyConfiguration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByHistoryConfiguration();

  /** Order by incidentState (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByIncidentState();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of incidents without tenant id is database-specific.
   */
  HistoricIncidentQuery orderByTenantId();

}
