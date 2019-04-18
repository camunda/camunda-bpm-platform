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
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * Represents one evaluation of a decision.
 *
 * @author Philipp Ossler
 * @author Ingo Richtsmeier
 *
 */
public interface HistoricDecisionInstance {

  /** The unique identifier of this historic decision instance. */
  String getId();

  /** The decision definition reference. */
  String getDecisionDefinitionId();

  /** The unique identifier of the decision definition */
  String getDecisionDefinitionKey();

  /** The name of the decision definition */
  String getDecisionDefinitionName();

  /** Time when the decision was evaluated. */
  Date getEvaluationTime();

  /** Time when the historic decision instance is to be removed. */
  Date getRemovalTime();

  /** The corresponding key of the process definition in case the decision was evaluated inside a process. */
  String getProcessDefinitionKey();

  /** The corresponding id of the process definition in case the decision was evaluated inside a process. */
  String getProcessDefinitionId();

  /** The corresponding process instance in case the decision was evaluated inside a process. */
  String getProcessInstanceId();

  /** The corresponding key of the case definition in case the decision was evaluated inside a case. */
  String getCaseDefinitionKey();

  /** The corresponding id of the case definition in case the decision was evaluated inside a case. */
  String getCaseDefinitionId();

  /** The corresponding case instance in case the decision was evaluated inside a case. */
  String getCaseInstanceId();

  /** The corresponding activity in case the decision was evaluated inside a process or a case. */
  String getActivityId();

  /** The corresponding activity instance in case the decision was evaluated inside a process or a case. */
  String getActivityInstanceId();

  /**
   * The user ID in case the decision was evaluated by an authenticated user using the decision service
   * outside of an execution context.
   */
  String getUserId();

  /**
   * The input values of the evaluated decision. The fetching of the input values must be enabled on the query.
   *
   * @throws ProcessEngineException if the input values are not fetched.
   *
   * @see HistoricDecisionInstanceQuery#includeInputs()
   */
  List<HistoricDecisionInputInstance> getInputs();

  /**
   * The output values of the evaluated decision. The fetching of the output values must be enabled on the query.
   *
   * @throws ProcessEngineException if the output values are not fetched.
   *
   * @see HistoricDecisionInstanceQuery#includeOutputs()
   */
  List<HistoricDecisionOutputInstance> getOutputs();

  /** The result of the collect operation if the hit policy 'collect' was used for the decision. */
  Double getCollectResultValue();

  /**
   * The unique identifier of the historic decision instance of the evaluated root decision.
   * Can be <code>null</code> if this instance is the root decision instance of the evaluation.
   */
  String getRootDecisionInstanceId();

  /**
   * The unique identifier of the root historic process instance of the evaluated root decision
   * in case the decision was evaluated inside a process, otherwise <code>null</code>.
   */
  String getRootProcessInstanceId();

  /**
   * The id of the related decision requirements definition. Can be
   * <code>null</code> if the decision has no relations to other decisions.
   */
  String getDecisionRequirementsDefinitionId();

  /**
   * The key of the related decision requirements definition. Can be
   * <code>null</code> if the decision has no relations to other decisions.
   */
  String getDecisionRequirementsDefinitionKey();

  /**
   * The id of the tenant this historic decision instance belongs to. Can be <code>null</code>
   * if the historic decision instance belongs to no single tenant.
   */
  String getTenantId();
}
