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

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.impl.dmn.cmd.EvaluateDecisionByIdCmd;
import org.camunda.bpm.engine.impl.dmn.cmd.EvaluateDecisionByKeyCmd;

/**
 * @author Philipp Ossler
 */
public class DecisionServiceImpl extends ServiceImpl implements DecisionService {

  @Override
  public DmnDecisionResult evaluateDecisionById(String decisionDefinitionId, Map<String, Object> variables) {
    return commandExecutor.execute(new EvaluateDecisionByIdCmd(decisionDefinitionId, variables));
  }

  @Override
  public DmnDecisionResult evaluateDecisionByKey(String decisionDefinitionKey, Map<String, Object> variables) {
    return commandExecutor.execute(new EvaluateDecisionByKeyCmd(decisionDefinitionKey, variables));
  }

  @Override
  public DmnDecisionResult evaluateDecisionByKeyAndVersion(String decisionDefinitionKey, Integer version, Map<String, Object> variables) {
    return commandExecutor.execute(new EvaluateDecisionByKeyCmd(decisionDefinitionKey, version, variables));
  }

}
