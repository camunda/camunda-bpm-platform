/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

  /** The corresponding key of the process definition in case the decision was evaluated inside a process. */
  String getProcessDefinitionKey();

  /** The corresponding id of the process definition in case the decision was evaluated inside a process. */
  String getProcessDefinitionId();

  /** The corresponding process instance in case the decision was evaluated inside a process. */
  String getProcessInstanceId();

  /** The corresponding activity in case the decision was evaluated inside a process. */
  String getActivityId();

  /** The corresponding activity instance in case the decision was evaluated inside a process. */
  String getActivityInstanceId();

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
}
