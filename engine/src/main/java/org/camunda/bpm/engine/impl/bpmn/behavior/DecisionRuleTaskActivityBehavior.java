/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.util.CallableElementUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;

public class DecisionRuleTaskActivityBehavior extends ScriptTaskActivityBehavior {

  protected BaseCallableElement callableElement;

  public DecisionRuleTaskActivityBehavior(String resultVariable) {
    super(null, resultVariable);
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    script = createScript(execution);
    super.execute(execution);
  }

  public BaseCallableElement getCallableElement() {
    return callableElement;
  }

  public void setCallableElement(BaseCallableElement callableElement) {
    this.callableElement = callableElement;
  }

  protected ExecutableScript createScript(ActivityExecution execution) {
    DecisionDefinition definition = CallableElementUtil.getDecisionDefinitionToCall(execution, getCallableElement());
    ScriptFactory scriptFactory = getScriptFactory();
    return scriptFactory.createScriptFromDecisionDefinition(definition);
  }

  protected ScriptFactory getScriptFactory() {
    return Context.getProcessEngineConfiguration().getScriptFactory();
  }

}
