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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class TransitionInstantiationCmd extends AbstractInstantiationCmd {

  protected String transitionId;

  public TransitionInstantiationCmd(String processInstanceId, String transitionId) {
    this(processInstanceId, transitionId, null);
  }

  public TransitionInstantiationCmd(String processInstanceId, String transitionId,
      String ancestorActivityInstanceId) {
    super(processInstanceId, ancestorActivityInstanceId);
    this.transitionId = transitionId;
  }

  protected ScopeImpl getTargetFlowScope(ProcessDefinitionImpl processDefinition) {
    TransitionImpl transition = processDefinition.findTransition(transitionId);
    return transition.getSource().getFlowScope();
  }

  protected CoreModelElement getTargetElement(ProcessDefinitionImpl processDefinition) {
    TransitionImpl transition = processDefinition.findTransition(transitionId);
    return transition;
  }
}
