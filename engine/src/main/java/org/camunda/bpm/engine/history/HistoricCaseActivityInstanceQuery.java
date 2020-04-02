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
 * Programmatic querying for {@link HistoricCaseActivityInstance}s.
 *
 * @author Sebastian Menski
 */
public interface HistoricCaseActivityInstanceQuery extends Query<HistoricCaseActivityInstanceQuery, HistoricCaseActivityInstance>{

  /** Only select historic case activity instances with the given id (primary key within history tables). */
  HistoricCaseActivityInstanceQuery caseActivityInstanceId(String caseActivityInstanceId);

  /** Only select historic case activity instances with one of the given case activity instance ids. */
  HistoricCaseActivityInstanceQuery caseActivityInstanceIdIn(String... caseActivityInstanceIds);

  /** Only select historic case activity instances for the given case execution */
  HistoricCaseActivityInstanceQuery caseExecutionId(String caseExecutionId);

  /** Only select historic case activity instances with the given case instance. */
  HistoricCaseActivityInstanceQuery caseInstanceId(String caseInstanceId);

  /** Only select historic case activity instances for the given case definition */
  HistoricCaseActivityInstanceQuery caseDefinitionId(String caseDefinitionId);

  /** Only select historic case activity instances for the given case activity (id from CMMN 1.0 XML) */
  HistoricCaseActivityInstanceQuery caseActivityId(String caseActivityId);

  /** Only select historic case activity instances with one of the given case activity ids. */
  HistoricCaseActivityInstanceQuery caseActivityIdIn(String... caseActivityIds);

  /** Only select historic case activity instances for activities with the given name */
  HistoricCaseActivityInstanceQuery caseActivityName(String caseActivityName);

  /** Only select historic case activity instances for activities with the given type */
  HistoricCaseActivityInstanceQuery caseActivityType(String caseActivityType);

  /** Only select historic case activity instances that were created before the given date. */
  HistoricCaseActivityInstanceQuery createdBefore(Date date);

  /** Only select historic case activity instances that were created after the given date. */
  HistoricCaseActivityInstanceQuery createdAfter(Date date);

  /** Only select historic case activity instances that were ended (ie. completed or terminated) before the given date. */
  HistoricCaseActivityInstanceQuery endedBefore(Date date);

  /** Only select historic case activity instances that were ended (ie. completed or terminated) after the given date. */
  HistoricCaseActivityInstanceQuery endedAfter(Date date);

  /** Only select historic case activity instances which are required. */
  HistoricCaseActivityInstanceQuery required();

  /** Only select historic case activity instances which are already ended (ie. completed or terminated). */
  HistoricCaseActivityInstanceQuery ended();

  /** Only select historic case activity instances which are not ended (ie. completed or terminated). */
  HistoricCaseActivityInstanceQuery notEnded();

  /** Only select historic case activity instances which are available */
  HistoricCaseActivityInstanceQuery available();

  /** Only select historic case activity instances which are enabled */
  HistoricCaseActivityInstanceQuery enabled();

  /** Only select historic case activity instances which are disabled */
  HistoricCaseActivityInstanceQuery disabled();

  /** Only select historic case activity instances which are active */
  HistoricCaseActivityInstanceQuery active();

  /** Only select historic case activity instances which are completed */
  HistoricCaseActivityInstanceQuery completed();

  /** Only select historic case activity instances which are terminated */
  HistoricCaseActivityInstanceQuery terminated();

  /** Only select historic case activity instances with one of the given tenant ids. */
  HistoricCaseActivityInstanceQuery tenantIdIn(String... tenantIds);

  /** Only selects historic case activity instances that have no tenant id. */
  HistoricCaseActivityInstanceQuery withoutTenantId();

  // ordering /////////////////////////////////////////////////////////////////
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByHistoricCaseActivityInstanceId();

  /** Order by caseInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByCaseInstanceId();

  /** Order by caseExecutionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByCaseExecutionId();

  /** Order by caseActivityId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByCaseActivityId();

  /** Order by caseActivityName (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByCaseActivityName();

  /** Order by caseActivityType (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByCaseActivityType();

  /** Order by create time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByHistoricCaseActivityInstanceCreateTime();

  /** Order by end time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByHistoricCaseActivityInstanceEndTime();

  /** Order by duration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByHistoricCaseActivityInstanceDuration();

  /** Order by caseDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricCaseActivityInstanceQuery orderByCaseDefinitionId();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of historic case activity instances without tenant id is database-specific.
   */
  HistoricCaseActivityInstanceQuery orderByTenantId();

}
