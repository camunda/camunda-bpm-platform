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

package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;

/**
 * The producer for DMN history events. The history event producer is
 * responsible for extracting data from the dmn engine and adding the data to a
 * {@link HistoryEvent}.
 *
 * @author Philipp Ossler
 *
 */
public interface DmnHistoryEventProducer {

  /**
   * Creates the history event fired when a decision is evaluated while execute
   * a process instance.
   *
   * @param execution
   *          the current execution
   * @param decisionTableEvaluationEvent
   *          the evaluation event
   * @return the history event
   *
   * @see #createDecisionEvaluatedEvt(DmnDecisionTableEvaluationEvent)
   */
  HistoryEvent createDecisionEvaluatedEvt(DelegateExecution execution, DmnDecisionTableEvaluationEvent decisionTableEvaluationEvent);

  /**
   * Creates the history event fired when a decision is evaluated while execute
   * a case instance.
   *
   * @param execution
   *          the current case execution
   * @param decisionTable
   *          the evaluated decision table
   * @param decisionTableResult
   *          the decision table evaluation result
   * @return the history event
   *
   * @see #createDecisionEvaluatedEvt(DmnDecisionTable, DmnDecisionTableResult)
   */
  HistoryEvent createDecisionEvaluatedEvt(DelegateCaseExecution execution, DmnDecisionTableEvaluationEvent decisionTableEvaluationEvent);

  /**
   * Creates the history event fired when a decision is evaluated. If the
   * decision is evaluated while execute a process instance then you should use
   * {@link #createDecisionEvaluatedEvt(DelegateExecution, DmnDecisionTableEvaluationEvent)} instead.
   *
   * @param decisionTableEvaluationEvent
   *          the evaluation event
   * @return the history event
   */
  HistoryEvent createDecisionEvaluatedEvt(DmnDecisionTableEvaluationEvent decisionTableEvaluationEvent);

}
