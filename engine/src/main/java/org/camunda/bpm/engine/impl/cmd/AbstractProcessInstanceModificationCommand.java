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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ActivityExecutionMapping;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractProcessInstanceModificationCommand implements Command<Void> {

  protected String processInstanceId;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public AbstractProcessInstanceModificationCommand(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }

  protected ActivityInstance findActivityInstance(ActivityInstance tree, String activityInstanceId) {
    if (activityInstanceId.equals(tree.getId())) {
      return tree;
    } else {
      for (ActivityInstance child : tree.getChildActivityInstances()) {
        ActivityInstance matchingChildInstance = findActivityInstance(child, activityInstanceId);
        if (matchingChildInstance != null) {
          return matchingChildInstance;
        }
      }
    }

    return null;
  }

  protected ScopeImpl getScopeForActivityInstance(ProcessDefinitionImpl processDefinition,
      ActivityInstance activityInstance) {
    String scopeId = activityInstance.getActivityId();

    if (processDefinition.getId().equals(scopeId)) {
      return processDefinition;
    }
    else {
      return processDefinition.findActivity(scopeId);
    }
  }

  protected ExecutionEntity getScopeExecutionForActivityInstance(ExecutionEntity processInstance,
      ActivityExecutionMapping mapping, ActivityInstance activityInstance) {
    ensureNotNull("activityInstance", activityInstance);

    ProcessDefinitionImpl processDefinition = processInstance.getProcessDefinition();
    ScopeImpl scope = getScopeForActivityInstance(processDefinition, activityInstance);

    Set<ExecutionEntity> executions = mapping.getExecutions(scope);
    Set<String> activityInstanceExecutions = new HashSet<String>(Arrays.asList(activityInstance.getExecutionIds()));

    // TODO: this is a hack around the activity instance tree
    // remove with fix of CAM-3574
    for (String activityInstanceExecutionId : activityInstance.getExecutionIds()) {
      ExecutionEntity execution = Context.getCommandContext()
          .getExecutionManager()
          .findExecutionById(activityInstanceExecutionId);
      if (execution.isConcurrent() && !execution.getExecutions().isEmpty()) {
        // concurrent executions have at most one child
        ExecutionEntity child = execution.getExecutions().get(0);
        activityInstanceExecutions.add(child.getId());
      }
    }

    // find the scope execution for the given activity instance
    Set<ExecutionEntity> retainedExecutionsForInstance = new HashSet<ExecutionEntity>();
    for (ExecutionEntity execution : executions) {
      if (activityInstanceExecutions.contains(execution.getId())) {
        retainedExecutionsForInstance.add(execution);
      }
    }

    if (retainedExecutionsForInstance.size() != 1) {
      throw new ProcessEngineException("There are " + retainedExecutionsForInstance.size()
          + " (!= 1) executions for activity instance " + activityInstance.getId());
    }

    return retainedExecutionsForInstance.iterator().next();
  }
}
