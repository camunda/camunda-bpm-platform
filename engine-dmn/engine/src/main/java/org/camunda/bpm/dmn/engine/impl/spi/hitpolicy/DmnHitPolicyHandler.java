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
package org.camunda.bpm.dmn.engine.impl.spi.hitpolicy;

import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.HitPolicyEntry;

/**
 * Handler for a DMN decision table hit policy.
 */
public interface DmnHitPolicyHandler {

  /**
   * Applies hit policy. Depending on the hit policy this can mean filtering and sorting of matching rules or
   * aggregating results.
   *
   * @param decisionTableEvaluationEvent the evaluation event of the decision table
   * @return the final evaluation result
   *
   * @throws DmnEngineException
   *           if the hit policy cannot be applied to the decision outputs
   */
  DmnDecisionTableEvaluationEvent apply(DmnDecisionTableEvaluationEvent decisionTableEvaluationEvent);

  /**
   * @return the implemented hit policy and aggregator
   */
  HitPolicyEntry getHitPolicyEntry();

}
