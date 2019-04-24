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
package org.camunda.bpm.engine.impl.history.producer;

import org.camunda.bpm.dmn.engine.delegate.DmnDecisionEvaluationEvent;
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
   * @param decisionEvaluationEvent
   *          the evaluation event
   * @return the history event
   *
   * @see #createDecisionEvaluatedEvt(DmnDecisionEvaluationEvent)
   */
  HistoryEvent createDecisionEvaluatedEvt(DelegateExecution execution, DmnDecisionEvaluationEvent decisionEvaluationEvent);

  /**
   * Creates the history event fired when a decision is evaluated while execute
   * a case instance.
   *
   * @param execution
   *          the current case execution
   * @param decisionEvaluationEvent
   *          the evaluation event
   * @return the history event
   *
   * @see #createDecisionEvaluatedEvt(DmnDecisionEvaluationEvent)
   */
  HistoryEvent createDecisionEvaluatedEvt(DelegateCaseExecution execution, DmnDecisionEvaluationEvent decisionEvaluationEvent);

  /**
   * Creates the history event fired when a decision is evaluated. If the
   * decision is evaluated while execute a process instance then you should use
   * {@link #createDecisionEvaluatedEvt(DelegateExecution, DmnDecisionEvaluationEvent)} instead.
   *
   * @param decisionEvaluationEvent
   *          the evaluation event
   * @return the history event
   */
  HistoryEvent createDecisionEvaluatedEvt(DmnDecisionEvaluationEvent decisionEvaluationEvent);

}
