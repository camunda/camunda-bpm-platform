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

package org.camunda.bpm.engine.impl;

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.dmn.DecisionEvaluationBuilder;
import org.camunda.bpm.engine.dmn.DecisionsEvaluationBuilder;
import org.camunda.bpm.engine.impl.dmn.DecisionEvaluationBuilderImpl;
import org.camunda.bpm.engine.impl.dmn.DecisionTableEvaluationBuilderImpl;

/**
 * @author Philipp Ossler
 */
public class DecisionServiceImpl extends ServiceImpl implements DecisionService {

  public DmnDecisionTableResult evaluateDecisionTableById(String decisionDefinitionId, Map<String, Object> variables) {
    return evaluateDecisionTableById(decisionDefinitionId)
        .variables(variables)
        .evaluate();
  }

  public DmnDecisionTableResult evaluateDecisionTableByKey(String decisionDefinitionKey, Map<String, Object> variables) {
    return evaluateDecisionTableByKey(decisionDefinitionKey)
        .variables(variables)
        .evaluate();
  }

  public DmnDecisionTableResult evaluateDecisionTableByKeyAndVersion(String decisionDefinitionKey, Integer version, Map<String, Object> variables) {
    return evaluateDecisionTableByKey(decisionDefinitionKey)
        .version(version)
        .variables(variables)
        .evaluate();
  }

  public DecisionEvaluationBuilder evaluateDecisionTableByKey(String decisionDefinitionKey) {
    return DecisionTableEvaluationBuilderImpl.evaluateDecisionTableByKey(commandExecutor, decisionDefinitionKey);
  }

  public DecisionEvaluationBuilder evaluateDecisionTableById(String decisionDefinitionId) {
    return DecisionTableEvaluationBuilderImpl.evaluateDecisionTableById(commandExecutor, decisionDefinitionId);
  }

  public DecisionsEvaluationBuilder evaluateDecisionByKey(String decisionDefinitionKey) {
    return DecisionEvaluationBuilderImpl.evaluateDecisionByKey(commandExecutor, decisionDefinitionKey);
  }

  public DecisionsEvaluationBuilder evaluateDecisionById(String decisionDefinitionId) {
    return DecisionEvaluationBuilderImpl.evaluateDecisionById(commandExecutor, decisionDefinitionId);
  }

}
