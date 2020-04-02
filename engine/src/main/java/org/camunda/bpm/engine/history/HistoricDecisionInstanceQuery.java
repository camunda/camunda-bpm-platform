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
 * Allows programmatic querying of {@link HistoricDecisionInstance}s.
 *
 * @author Philipp Ossler
 */
public interface HistoricDecisionInstanceQuery extends Query<HistoricDecisionInstanceQuery, HistoricDecisionInstance> {

  /** Only select historic decision instances with the given decision instance id. */
  HistoricDecisionInstanceQuery decisionInstanceId(String decisionInstanceId);

  /** Only select historic decision instances whose id is in the given list of ids. */
  HistoricDecisionInstanceQuery decisionInstanceIdIn(String... decisionInstanceIdIn);

  /** Only select historic decision instances for the given decision definition */
  HistoricDecisionInstanceQuery decisionDefinitionId(String decisionDefinitionId);

  /** Only select historic decision instances for the given decision definitions */
  HistoricDecisionInstanceQuery decisionDefinitionIdIn(String... decisionDefinitionIdIn);

  /** Only select historic decision instances with the given key of the decision definition. */
  HistoricDecisionInstanceQuery decisionDefinitionKey(String decisionDefinitionKey);

  /** Only select historic decision instances with the given keys of the decision definition. */
  HistoricDecisionInstanceQuery decisionDefinitionKeyIn(String... decisionDefinitionKeyIn);

  /** Only select historic decision instances with the given name of the decision definition. */
  HistoricDecisionInstanceQuery decisionDefinitionName(String decisionDefinitionName);

  /**
   * Only select historic decision instances with the given name of the decision definition using LIKE construct.
   */
  HistoricDecisionInstanceQuery decisionDefinitionNameLike(String decisionDefinitionNameLike);

  /** Only select historic decision instances that are evaluated inside a process
   * with the given process definition key. */
  HistoricDecisionInstanceQuery processDefinitionKey(String processDefinitionKey);

  /** Only select historic decision instances that are evaluated inside a process
   * with the given process definition id. */
  HistoricDecisionInstanceQuery processDefinitionId(String processDefinitionId);

  /** Only select historic decision instances that are evaluated inside a process
   * with the given process instance id. */
  HistoricDecisionInstanceQuery processInstanceId(String processInstanceId);

  /** Only select historic decision instances that are evaluated inside a case
   * with the given case definition key. */
  HistoricDecisionInstanceQuery caseDefinitionKey(String caseDefinitionKey);

  /** Only select historic decision instances that are evaluated inside a case
   * with the given case definition id. */
  HistoricDecisionInstanceQuery caseDefinitionId(String caseDefinitionId);

  /** Only select historic decision instances that are evaluated inside a case
   * with the given case instance id. */
  HistoricDecisionInstanceQuery caseInstanceId(String caseInstanceId);

  /** Only select historic decision instances that are evaluated inside a process or a case
   * which have one of the activity ids. */
  HistoricDecisionInstanceQuery activityIdIn(String... activityIds);

  /** Only select historic decision instances that are evaluated inside a process or a case
   * which have one of the activity instance ids. */
  HistoricDecisionInstanceQuery activityInstanceIdIn(String... activityInstanceIds);

  /** Only select historic decision instances that were evaluated before the given date. */
  HistoricDecisionInstanceQuery evaluatedBefore(Date date);

  /** Only select historic decision instances that were evaluated after the given date. */
  HistoricDecisionInstanceQuery evaluatedAfter(Date date);

  /** Only select historic decision instances that were evaluated by the user with the given user ID.
   * <p> The user ID is saved for decisions which are evaluated by a authenticated user without a process or
   * case instance */
  HistoricDecisionInstanceQuery userId(String userId);

  /** Enable fetching {@link HistoricDecisionInputInstance} of evaluated decision. */
  HistoricDecisionInstanceQuery includeInputs();

  /** Enable fetching {@link HistoricDecisionOutputInstance} of evaluated decision. */
  HistoricDecisionInstanceQuery includeOutputs();

  /** Disable fetching of byte array input and output values. By default, the query will fetch the value of a byte array.
   * By calling this method you can prevent the values of (potentially large) blob data chunks to be fetched. */
  HistoricDecisionInstanceQuery disableBinaryFetching();

  /** Disable deserialization of input and output values that are custom objects. By default, the query
   * will attempt to deserialize the value of these variables. By calling this method you can
   * prevent such attempts in environments where their classes are not available.
   * Independent of this setting, variable serialized values are accessible. */
  HistoricDecisionInstanceQuery disableCustomObjectDeserialization();

  /**
   * Only select historic decision instances with a given root historic decision
   * instance id. This also includes the historic decision instance with the
   * given id.
   */
  HistoricDecisionInstanceQuery rootDecisionInstanceId(String decisionInstanceId);

  /** Only select historic decision instances that are the root decision instance of an evaluation. */
  HistoricDecisionInstanceQuery rootDecisionInstancesOnly();

  /** Only select historic decision instances that belongs to a decision requirements definition with the given id. */
  HistoricDecisionInstanceQuery decisionRequirementsDefinitionId(String decisionRequirementsDefinitionId);

  /** Only select historic decision instances that belongs to a decision requirements definition with the given key. */
  HistoricDecisionInstanceQuery decisionRequirementsDefinitionKey(String decisionRequirementsDefinitionKey);

  /** Only select historic decision instances with one of the given tenant ids. */
  HistoricDecisionInstanceQuery tenantIdIn(String... tenantIds);

  /** Only selects historic decision instances that have no tenant id. */
  HistoricDecisionInstanceQuery withoutTenantId();

  /** Order by the time when the decisions was evaluated
   * (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricDecisionInstanceQuery orderByEvaluationTime();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of historic decision instances without tenant id is database-specific.
   */
  HistoricDecisionInstanceQuery orderByTenantId();

}
