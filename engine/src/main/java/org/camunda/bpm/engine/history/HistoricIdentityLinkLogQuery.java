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

import java.util.Date;

import org.camunda.bpm.engine.query.Query;

/**
 * @author Deivarayan Azhagappan
 */
public interface HistoricIdentityLinkLogQuery extends Query<HistoricIdentityLinkLogQuery, HistoricIdentityLinkLog> {

  /**
   * Only select historic identity links which have the date before the give date.
   **/
  HistoricIdentityLinkLogQuery dateBefore(Date dateBefore);

  /**
   * Only select historic identity links which have the date after the give date.
   **/
  HistoricIdentityLinkLogQuery dateAfter(Date dateAfter);

  /**
   * Only select historic identity links which have the given identity link type.
   **/
  HistoricIdentityLinkLogQuery type(String type);

  /**
   * Only select historic identity links which have the given user id.
   **/
  HistoricIdentityLinkLogQuery userId(String userId);

  /**
   * Only select historic identity links which have the given group id.
   **/
  HistoricIdentityLinkLogQuery groupId(String groupId);

  /**
   * Only select historic identity links which have the given task id.
   **/
  HistoricIdentityLinkLogQuery taskId(String taskId);

  /**
   * Only select historic identity links which have the given process definition id.
   **/
  HistoricIdentityLinkLogQuery processDefinitionId(String processDefinitionId);

  /**
   * Only select historic identity links which have the given process definition key.
   **/
  HistoricIdentityLinkLogQuery processDefinitionKey(String processDefinitionKey);

  /**
   * Only select historic identity links which have the given operation type (add/delete).
   **/
  HistoricIdentityLinkLogQuery operationType(String operationType);

  /**
   * Only select historic identity links which have the given assigner id.
   **/
  HistoricIdentityLinkLogQuery assignerId(String assignerId);

  /**
   * Only select historic identity links which have the given tenant id.
   **/
  HistoricIdentityLinkLogQuery tenantIdIn(String... tenantId);

  /** Only selects historic job log entries that have no tenant id. */
  HistoricIdentityLinkLogQuery withoutTenantId();

  /**
   * Order by time (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByTime();

  /**
   * Order by type (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByType();

  /**
   * Order by userId (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByUserId();

  /**
   * Order by groupId (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByGroupId();

  /**
   * Order by taskId (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByTaskId();

  /**
   * Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByProcessDefinitionId();

  /**
   * Order by processDefinitionKey (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByProcessDefinitionKey();

  /**
   * Order by operationType (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByOperationType();

  /**
   * Order by assignerId (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByAssignerId();

  /**
   * Order by tenantId (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  HistoricIdentityLinkLogQuery orderByTenantId();

}
