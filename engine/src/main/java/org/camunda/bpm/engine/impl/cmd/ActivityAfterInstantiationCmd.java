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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityAfterInstantiationCmd extends AbstractInstantiationCmd {

  protected String activityId;

  public ActivityAfterInstantiationCmd(String processInstanceId, String activityId) {
    this(processInstanceId, activityId, null);
  }

  public ActivityAfterInstantiationCmd(String processInstanceId, String activityId,
      String ancestorActivityInstanceId) {
    super(processInstanceId, ancestorActivityInstanceId);
    this.activityId = activityId;
  }

  public Void execute(CommandContext commandContext) {
    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    ProcessDefinitionImpl processDefinition = processInstance.getProcessDefinition();

    PvmActivity activity = processDefinition.findActivity(activityId);

    if (activity.getOutgoingTransitions().isEmpty()) {
      throw new ProcessEngineException("Cannot start after activity " + activityId + "; activity "
          + "has no outgoing sequence flow to take");
    }
    else if (activity.getOutgoingTransitions().size() > 1) {
      throw new ProcessEngineException("Cannot start after activity " + activityId + "; "
          + "activity has more than one outgoing sequence flow");
    }

    return super.execute(commandContext);
  }

  protected ScopeImpl getTargetFlowScope(ProcessDefinitionImpl processDefinition) {
    PvmActivity sourceActivity = processDefinition.findActivity(activityId);
    TransitionImpl transition = (TransitionImpl) sourceActivity.getOutgoingTransitions().get(0);

    return transition.getDestination().getFlowScope();
  }

  protected CoreModelElement getTargetElement(ProcessDefinitionImpl processDefinition) {
    PvmActivity sourceActivity = processDefinition.findActivity(activityId);
    TransitionImpl transition = (TransitionImpl) sourceActivity.getOutgoingTransitions().get(0);

    return transition;
  }
}
