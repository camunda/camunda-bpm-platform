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
package org.camunda.bpm.dmn.engine.impl.spi.transform;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRequirementsGraph;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.Rule;

/**
 * Listener for a DMN transformation
 */
public interface DmnTransformListener {

  /**
   * Notified after a DMN decision was transformed
   *
   * @param decision the decision from the DMN model instance
   * @param dmnDecision the transformed {@link DmnDecision}
   */
  void transformDecision(Decision decision, DmnDecision dmnDecision);

  /**
   * Notified after a DMN decision table input was transformed
   *
   * @param input the input from the DMN model instance
   * @param dmnInput the transformed {@link DmnDecisionTableInputImpl}
   */
  void transformDecisionTableInput(Input input, DmnDecisionTableInputImpl dmnInput);

  /**
   * Notified after a DMN decision table output was transformed
   *
   * @param output the output from the DMN model instance
   * @param dmnOutput the transformed {@link DmnDecisionTableOutputImpl}
   */
  void transformDecisionTableOutput(Output output, DmnDecisionTableOutputImpl dmnOutput);

  /**
   * Notified after a DMN decision table rule was transformed
   *
   * @param rule the rule from the DMN model instance
   * @param dmnRule the transformed {@link DmnDecisionTableRuleImpl}
   */
  void transformDecisionTableRule(Rule rule, DmnDecisionTableRuleImpl dmnRule);

  /**
   * Notified after a Decision Requirements Graph was transformed
   * 
   * @param definitions
   * @param dmnDecisionGraph
   */
  void transformDecisionRequirementsGraph(Definitions definitions, DmnDecisionRequirementsGraph dmnDecisionRequirementsGraph);
  
}
