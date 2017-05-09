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
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityAfterInstantiationCmd extends AbstractInstantiationCmd {

  protected String activityId;

  public ActivityAfterInstantiationCmd(String activityId) {
    this(null, activityId);
  }

  public ActivityAfterInstantiationCmd(String processInstanceId, String activityId) {
    this(processInstanceId, activityId, null);
  }

  public ActivityAfterInstantiationCmd(String processInstanceId, String activityId,
      String ancestorActivityInstanceId) {
    super(processInstanceId, ancestorActivityInstanceId);
    this.activityId = activityId;
  }

  @Override
  protected ScopeImpl getTargetFlowScope(ProcessDefinitionImpl processDefinition) {
    TransitionImpl transition = findTransition(processDefinition);

    return transition.getDestination().getFlowScope();
  }

  @Override
  protected CoreModelElement getTargetElement(ProcessDefinitionImpl processDefinition) {
    return findTransition(processDefinition);
  }

  protected TransitionImpl findTransition(ProcessDefinitionImpl processDefinition) {
    PvmActivity activity = processDefinition.findActivity(activityId);

    EnsureUtil.ensureNotNull(NotValidException.class,
        describeFailure("Activity '" + activityId + "' does not exist"),
        "activity",
        activity);

    if (activity.getOutgoingTransitions().isEmpty()) {
      throw new ProcessEngineException("Cannot start after activity " + activityId + "; activity "
          + "has no outgoing sequence flow to take");
    }
    else if (activity.getOutgoingTransitions().size() > 1) {
      throw new ProcessEngineException("Cannot start after activity " + activityId + "; "
          + "activity has more than one outgoing sequence flow");
    }

    return (TransitionImpl) activity.getOutgoingTransitions().get(0);
  }

  @Override
  public String getTargetElementId() {
    return activityId;
  }

  @Override
  protected String describe() {
    StringBuilder sb = new StringBuilder();
    sb.append("Start after activity '");
    sb.append(activityId);
    sb.append("'");
    if (ancestorActivityInstanceId != null) {
      sb.append(" with ancestor activity instance '");
      sb.append(ancestorActivityInstanceId);
      sb.append("'");
    }

    return sb.toString();
  }
}
