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
package org.camunda.bpm.engine.runtime;

import org.camunda.bpm.engine.query.Query;

import java.util.Date;

/**
 * @author roman.smirnov
 */
public interface IncidentQuery extends Query<IncidentQuery, Incident> {

  /** Only select incidents which have the given id. **/
  IncidentQuery incidentId(String incidentId);

  /** Only select incidents which have the given incident type. **/
  IncidentQuery incidentType(String incidentType);

  /** Only select incidents which have the given incident message. **/
  IncidentQuery incidentMessage(String incidentMessage);

  /**
   * Only select incidents which incident message is like the given value.
   *
   * @param incidentMessageLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  IncidentQuery incidentMessageLike(String incidentMessageLike);

  /** Only select incidents which have the given process definition id. **/
  IncidentQuery processDefinitionId(String processDefinitionId);

  /** Only select incidents which have one of the given process definition keys. **/
  IncidentQuery processDefinitionKeyIn(String... processDefinitionKeys);

  /** Only select incidents which have the given process instance id. **/
  IncidentQuery processInstanceId(String processInstanceId);

  /** Only select incidents with the given id. **/
  IncidentQuery executionId(String executionId);

  /** Only select incidents which have an incidentTimestamp date before the given date **/
  IncidentQuery incidentTimestampBefore(Date incidentTimestampBefore);

  /** Only select incidents which have an incidentTimestamp date after the given date **/
  IncidentQuery incidentTimestampAfter(Date incidentTimestampAfter);

  /** Only select incidents which contain an activity with the given id. **/
  IncidentQuery activityId(String activityId);

  /** Only select incidents which were created due to a failure at an activity with the given id. **/
  IncidentQuery failedActivityId(String activityId);

  /** Only select incidents which contain the id of the cause incident. **/
  IncidentQuery causeIncidentId(String causeIncidentId);

  /** Only select incidents which contain the id of the root cause incident. **/
  IncidentQuery rootCauseIncidentId(String rootCauseIncidentId);

  /** Only select incidents which contain the configuration. **/
  IncidentQuery configuration(String configuration);

  /** Only select incidents that belong to one of the given tenant ids. */
  IncidentQuery tenantIdIn(String... tenantIds);

  /** Only select incidents that belong to one of the given job definition ids. */
  IncidentQuery jobDefinitionIdIn(String... jobDefinitionIds);

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByIncidentId();

  /** Order by incidentTimestamp (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByIncidentTimestamp();

  /** Order by incident message (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByIncidentMessage();

  /** Order by incidentType (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByIncidentType();

  /** Order by executionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByExecutionId();

  /** Order by activityId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByActivityId();

  /** Order by processInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByProcessInstanceId();

  /** Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByProcessDefinitionId();

  /** Order by causeIncidentId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByCauseIncidentId();

  /** Order by rootCauseIncidentId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByRootCauseIncidentId();

  /** Order by configuration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByConfiguration();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of incidents without tenant id is database-specific.
   */
  IncidentQuery orderByTenantId();

}

