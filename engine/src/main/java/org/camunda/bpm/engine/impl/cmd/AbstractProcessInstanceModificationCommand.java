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
import org.camunda.bpm.engine.impl.ActivityExecutionTreeMapping;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;

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

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
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

  protected TransitionInstance findTransitionInstance(ActivityInstance tree, String transitionInstanceId) {
    for (TransitionInstance childTransitionInstance : tree.getChildTransitionInstances()) {
      if (matchesRequestedTransitionInstance(childTransitionInstance, transitionInstanceId)) {
        return childTransitionInstance;
      }
    }

    for (ActivityInstance child : tree.getChildActivityInstances()) {
      TransitionInstance matchingChildInstance = findTransitionInstance(child, transitionInstanceId);
      if (matchingChildInstance != null) {
        return matchingChildInstance;
      }
    }

    return null;
  }

  protected boolean matchesRequestedTransitionInstance(TransitionInstance instance, String queryInstanceId) {
    boolean match = instance.getId().equals(queryInstanceId);

    // check if the execution queried for has been replaced by the given instance
    // => if yes, given instance is matched
    // this is a fix for CAM-4090 to tolerate inconsistent transition instance ids as described in CAM-4143
    if (!match) {
      // note: execution id = transition instance id
      ExecutionEntity cachedExecution = Context.getCommandContext()
          .getDbEntityManager()
          .getCachedEntity(ExecutionEntity.class, queryInstanceId);

      // follow the links of execution replacement;
      // note: this can be at most two hops:
      // case 1:
      //   the query execution is the scope execution
      //     => tree may have expanded meanwhile
      //     => scope execution references replacing execution directly (one hop)
      //
      // case 2:
      //   the query execution is a concurrent execution
      //     => tree may have compacted meanwhile
      //     => concurrent execution references scope execution directly (one hop)
      //
      // case 3:
      //   the query execution is a concurrent execution
      //     => tree may have compacted/expanded/compacted/../expanded any number of times
      //     => the concurrent execution has been removed and therefore references the scope execution (first hop)
      //     => the scope execution may have been replaced itself again with another concurrent execution (second hop)
      //   note that the scope execution may have a long "history" of replacements, but only the last replacement is relevant here
      if (cachedExecution != null) {
        ExecutionEntity replacingExecution = cachedExecution.resolveReplacedBy();

        if (replacingExecution != null) {
          match = replacingExecution.getId().equals(instance.getId());
        }
      }
    }

    return match;
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
      ActivityExecutionTreeMapping mapping, ActivityInstance activityInstance) {
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
      if (execution.isConcurrent() && execution.hasChildren()) {
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

  protected String describeFailure(String detailMessage) {
    return "Cannot perform instruction: " + describe() + "; " + detailMessage;
  }

  protected abstract String describe();

  public String toString() {
    return describe();
  }
}
