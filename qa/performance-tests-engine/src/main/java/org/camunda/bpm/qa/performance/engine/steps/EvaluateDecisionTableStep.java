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
package org.camunda.bpm.qa.performance.engine.steps;

import java.util.Map;

import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestRunContext;
import org.camunda.bpm.qa.performance.engine.framework.PerfTestStepBehavior;

/**
 * Evaluate a decision table using the DecisionService of the engine.
 *
 * @author Philipp Ossler
 *
 */
public class EvaluateDecisionTableStep extends ProcessEngineAwareStep implements PerfTestStepBehavior {

  protected final String decisionDefinitionKey;
  protected final Map<String, Object> variables;

  public EvaluateDecisionTableStep(ProcessEngine engine, String decisionDefinitionKey, Map<String, Object> variables) {
    super(engine);

    this.decisionDefinitionKey = decisionDefinitionKey;
    this.variables = variables;
  }

  @Override
  public void execute(PerfTestRunContext context) {
    DecisionService decisionService = processEngine.getDecisionService();

    decisionService.evaluateDecisionTableByKey(decisionDefinitionKey, variables);
  }

}
