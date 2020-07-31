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
import org.camunda.bpm.engine.runtime.ProcessInstance;


/**
 * Programmatic querying for {@link HistoricActivityInstance}s.
 *
 * @author Tom Baeyens
 */
public interface HistoricActivityInstanceQuery extends Query<HistoricActivityInstanceQuery, HistoricActivityInstance>{

  /** Only select historic activity instances with the given id (primary key within history tables). */
  HistoricActivityInstanceQuery activityInstanceId(String activityInstanceId);

  /** Only select historic activity instances with the given process instance.
   * {@link ProcessInstance ) ids and {@link HistoricProcessInstance} ids match. */
  HistoricActivityInstanceQuery processInstanceId(String processInstanceId);

  /** Only select historic activity instances for the given process definition */
  HistoricActivityInstanceQuery processDefinitionId(String processDefinitionId);

  /** Only select historic activity instances for the given execution */
  HistoricActivityInstanceQuery executionId(String executionId);

  /** Only select historic activity instances for the given activity (id from BPMN 2.0 XML) */
  HistoricActivityInstanceQuery activityId(String activityId);

  /** Only select historic activity instances for activities with the given name */
  HistoricActivityInstanceQuery activityName(String activityName);

  /**
   * Only select historic activity instances for activities which activityName is like the given value.
   *
   * @param activityNameLike The string can include the wildcard character '%' to express
   *    like-strategy: starts with (string%), ends with (%string) or contains (%string%).
   */
  HistoricActivityInstanceQuery activityNameLike(String activityNameLike);

  /** Only select historic activity instances for activities with the given activity type */
  HistoricActivityInstanceQuery activityType(String activityType);

  /** Only select historic activity instances for userTask activities assigned to the given user */
  HistoricActivityInstanceQuery taskAssignee(String userId);

  /** Only select historic activity instances that are finished. */
  HistoricActivityInstanceQuery finished();

  /** Only select historic activity instances that are not finished yet. */
  HistoricActivityInstanceQuery unfinished();

  /** Only select historic activity instances that complete a BPMN scope */
  HistoricActivityInstanceQuery completeScope();

  /** Only select historic activity instances that got canceled */
  HistoricActivityInstanceQuery canceled();

  /** Only select historic activity instances that were started before the given date. */
  HistoricActivityInstanceQuery startedBefore(Date date);

  /** Only select historic activity instances that were started after the given date. */
  HistoricActivityInstanceQuery startedAfter(Date date);

  /** Only select historic activity instances that were started before the given date. */
  HistoricActivityInstanceQuery finishedBefore(Date date);

  /** Only select historic activity instances that were started after the given date. */
  HistoricActivityInstanceQuery finishedAfter(Date date);

  // ordering /////////////////////////////////////////////////////////////////
  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceId();

  /** Order by processInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByProcessInstanceId();

  /** Order by executionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByExecutionId();

  /** Order by activityId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByActivityId();

  /** Order by activityName (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByActivityName();

  /** Order by activityType (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByActivityType();

  /** Order by start (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceStartTime();

  /** Order by end (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceEndTime();

  /** Order by duration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceDuration();

  /** Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricActivityInstanceQuery orderByProcessDefinitionId();

  /**
   * <p>Sort the {@link HistoricActivityInstance activity instances} in the order in which
   * they occurred (ie. started) and needs to be followed by {@link #asc()} or {@link #desc()}.</p>
   *
   * <p>The set of all {@link HistoricActivityInstance activity instances} is a <strong>partially
   * ordered set</strong>. At a BPMN level this means that instances of concurrent activities (example:
   * activities on different parallel branched after a parallel gateway) cannot be compared to each other.
   * Instances of activities which are part of happens-before relation at the BPMN level will be ordered
   * in respect to that relation.</p>
   *
   * <p>Technically this means that {@link HistoricActivityInstance activity instances}
   * with different {@link HistoricActivityInstance#getExecutionId() execution ids} are
   * <strong>incomparable</strong>. Only {@link HistoricActivityInstance activity instances} with
   * the same {@link HistoricActivityInstance#getExecutionId() execution id} can be <strong>totally
   * ordered</strong> by using {@link #executionId(String)} and {@link #orderPartiallyByOccurrence()}
   * which will return a result set ordered by its occurrence.</p>
   *
   * @since 7.3
   */
  HistoricActivityInstanceQuery orderPartiallyByOccurrence();

  /** Only select historic activity instances with one of the given tenant ids. */
  HistoricActivityInstanceQuery tenantIdIn(String... tenantIds);

  /** Only selects historic activity instances that have no tenant id. */
  HistoricActivityInstanceQuery withoutTenantId();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of historic activity instances without tenant id is database-specific.
   */
  HistoricActivityInstanceQuery orderByTenantId();

}
