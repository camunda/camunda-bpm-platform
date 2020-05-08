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
package org.camunda.bpm.dmn.engine.impl.evaluation;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionLogicEvaluationEvent;
import org.camunda.bpm.engine.variable.context.VariableContext;

/**
 * Evaluates decisions with a specific kind of decision logic and generates the
 * result.
 */
public interface DmnDecisionLogicEvaluationHandler {

  /**
   * Evaluate a decision with the given {@link VariableContext}.
   *
   * @param decision
   *          the decision to evaluate
   * @param variableContext
   *          the available variable context
   * @return the event which represents the evaluation
   */
  DmnDecisionLogicEvaluationEvent evaluate(DmnDecision decision, VariableContext variableContext);

  /**
   * Generates the decision evaluation result of the given event.
   *
   * @param event
   *          which represents the evaluation
   * @return the result of the decision evaluation
   */
  DmnDecisionResult generateDecisionResult(DmnDecisionLogicEvaluationEvent event);

}
