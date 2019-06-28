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
package org.camunda.bpm.dmn.engine.delegate;

import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.dmn.HitPolicy;

/**
 * Event which represents the evaluation of a decision table
 */
public interface DmnDecisionTableEvaluationEvent extends DmnDecisionLogicEvaluationEvent {

  /**
   * @return the evaluated decision table
   */
  DmnDecision getDecisionTable();

  /**
   * @return the inputs on which the decision table was evaluated
   */
  List<DmnEvaluatedInput> getInputs();

  /**
   * @return the matching rules of the decision table evaluation
   */
  List<DmnEvaluatedDecisionRule> getMatchingRules();

  /**
   * @return the result name of the collect operation if the {@link HitPolicy#COLLECT} was used with an aggregator otherwise null
   */
  String getCollectResultName();

  /**
   * @return the result value of the collect operation if the {@link HitPolicy#COLLECT} was used with an aggregator otherwise null
   */
  TypedValue getCollectResultValue();

  /**
   * @return the number of executed decision elements during the evaluation
   */
  long getExecutedDecisionElements();

}
