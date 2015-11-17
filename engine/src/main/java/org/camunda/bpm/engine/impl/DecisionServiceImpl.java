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
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.exception.dmn.DecisionDefinitionNotFoundException;
import org.camunda.bpm.engine.impl.dmn.cmd.EvaluateDecisionByIdCmd;
import org.camunda.bpm.engine.impl.dmn.cmd.EvaluateDecisionByKeyCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;

/**
 * @author Philipp Ossler
 */
public class DecisionServiceImpl extends ServiceImpl implements DecisionService {

  public DmnDecisionTableResult evaluateDecisionTableById(String decisionDefinitionId, Map<String, Object> variables) {
    return evaluateDecisionTable(new EvaluateDecisionByIdCmd(decisionDefinitionId, variables));
  }

  public DmnDecisionTableResult evaluateDecisionTableByKey(String decisionDefinitionKey, Map<String, Object> variables) {
    return evaluateDecisionTable(new EvaluateDecisionByKeyCmd(decisionDefinitionKey, variables));
  }

  public DmnDecisionTableResult evaluateDecisionTableByKeyAndVersion(String decisionDefinitionKey, Integer version, Map<String, Object> variables) {
    return evaluateDecisionTable(new EvaluateDecisionByKeyCmd(decisionDefinitionKey, version, variables));
  }

  protected DmnDecisionTableResult evaluateDecisionTable(Command<DmnDecisionTableResult> cmd) {
    try {
      return commandExecutor.execute(cmd);
    }
    catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);
    }
    catch (DecisionDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    }
  }

}
