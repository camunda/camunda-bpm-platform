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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.CompensationBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * Maps an activity (plain activities + their containing flow scopes) to the scope executions
 * that are executing them. For every instance of a scope, there is one such execution.
 *
 * @author Thorben Lindhauer
 */
public class ActivityExecutionTreeMapping {

  protected Map<ScopeImpl, Set<ExecutionEntity>> activityExecutionMapping;
  protected CommandContext commandContext;
  protected String processInstanceId;

  public ActivityExecutionTreeMapping(CommandContext commandContext, String processInstanceId) {
    this.commandContext = commandContext;
    this.processInstanceId = processInstanceId;
    this.activityExecutionMapping = new HashMap<ScopeImpl, Set<ExecutionEntity>>();

    initialize();
  }

  protected void submitExecution(ExecutionEntity execution, ScopeImpl scope) {
    getExecutions(scope).add(execution);
  }

  public Set<ExecutionEntity> getExecutions(ScopeImpl activity) {
    Set<ExecutionEntity> executionsForActivity = activityExecutionMapping.get(activity);
    if (executionsForActivity == null) {
      executionsForActivity = new HashSet<ExecutionEntity>();
      activityExecutionMapping.put(activity, executionsForActivity);
    }

    return executionsForActivity;
  }

  protected void initialize() {
    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

    List<ExecutionEntity> executions = fetchExecutionsForProcessInstance(processInstance);
    executions.add(processInstance);

    List<ExecutionEntity> leaves = findLeaves(executions);

    assignExecutionsToActivities(leaves);
  }

  protected void assignExecutionsToActivities(List<ExecutionEntity> leaves) {
    for (ExecutionEntity leaf : leaves) {
      ScopeImpl activity = leaf.getActivity();

      if (activity != null) {
        if (leaf.getActivityInstanceId() != null) {
          EnsureUtil.ensureNotNull("activity", activity);
          submitExecution(leaf, activity);
        }
        mergeScopeExecutions(leaf);


      }
      else if (leaf.isProcessInstanceExecution()) {
        submitExecution(leaf, leaf.getProcessDefinition());
      }
    }
  }

  protected void mergeScopeExecutions(ExecutionEntity leaf) {
    Map<ScopeImpl, PvmExecutionImpl> mapping = leaf.createActivityExecutionMapping();

    for (Map.Entry<ScopeImpl, PvmExecutionImpl> mappingEntry : mapping.entrySet()) {
      ScopeImpl scope = mappingEntry.getKey();
      ExecutionEntity scopeExecution = (ExecutionEntity) mappingEntry.getValue();

      submitExecution(scopeExecution, scope);
    }


  }

  protected List<ExecutionEntity> fetchExecutionsForProcessInstance(ExecutionEntity execution) {
    List<ExecutionEntity> executions = new ArrayList<ExecutionEntity>();
    executions.addAll(execution.getExecutions());
    for (ExecutionEntity child : execution.getExecutions()) {
      executions.addAll(fetchExecutionsForProcessInstance(child));
    }

    return executions;
  }

  protected List<ExecutionEntity> findLeaves(List<ExecutionEntity> executions) {
    List<ExecutionEntity> leaves = new ArrayList<ExecutionEntity>();

    for (ExecutionEntity execution : executions) {
      if (isLeaf(execution)) {
        leaves.add(execution);
      }
    }

    return leaves;
  }

  /**
   * event-scope executions are not considered in this mapping and must be ignored
   */
  protected boolean isLeaf(ExecutionEntity execution) {
    if (CompensationBehavior.isCompensationThrowing(execution)) {
      return true;
    }
    else {
      return !execution.isEventScope() && execution.getNonEventScopeExecutions().isEmpty();
    }
  }
}
