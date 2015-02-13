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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ActivityExecutionMapping;
import org.camunda.bpm.engine.impl.ActivityInstantiationInstruction;
import org.camunda.bpm.engine.impl.ProcessInstanceModificationBuilderImpl;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * @author Thorben Lindhauer
 *
 */
public class ModifyProcessInstanceCmd implements Command<Void> {

  protected ProcessInstanceModificationBuilderImpl builder;

  public ModifyProcessInstanceCmd(ProcessInstanceModificationBuilderImpl processInstanceModificationBuilder) {
    this.builder = processInstanceModificationBuilder;
  }

  public Void execute(CommandContext commandContext) {
    String processInstanceId = builder.getProcessInstanceId();
    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);

    ProcessDefinitionImpl processDefinition = processInstance.getProcessDefinition();

    for (ActivityInstantiationInstruction startInstruction : builder.getActivitiesToStartBefore()) {
      ActivityImpl activity = processDefinition.findActivity(startInstruction.getActivityId());

      EnsureUtil.ensureNotNull("activity", activity);

      // rebuild the mapping because the execution tree changes with every iteration
      ActivityExecutionMapping mapping = new ActivityExecutionMapping(commandContext, processInstanceId);

      // before instantiating an activity, two things have to be determined:
      //
      // activityStack:
      // For the activity to instantiate, we build a stack of parent flow scopes
      // for which no executions exist yet and that have to be instantiated
      //
      // scopeExecution:
      // The execution of the first parent/ancestor flow scope that has an execution.
      // This is typically the execution under which a new sub tree has to be created

      List<PvmActivity> activitiesToInstantiate = new ArrayList<PvmActivity>();
      activitiesToInstantiate.add(activity);

      // builds the activity stack of flow scopes for which no executions exist yet
      ScopeImpl flowScope = activity.getFlowScope();

      Set<ExecutionEntity> flowScopeExecutions = mapping.getExecutions(flowScope);
      while (flowScopeExecutions.isEmpty()) {
        ActivityImpl flowScopeActivity = (ActivityImpl) flowScope;
        activitiesToInstantiate.add(flowScopeActivity);

        flowScope = flowScopeActivity.getFlowScope();
        flowScopeExecutions = mapping.getExecutions(flowScope);
      }

      if (flowScopeExecutions.size() > 1) {
        throw new ProcessEngineException("Execution is ambiguous for activity " + flowScope);
      }

      Collections.reverse(activitiesToInstantiate);
      ExecutionEntity scopeExecution = flowScopeExecutions.iterator().next();

      // We have to make a distinction between
      // - "regular" activities for which the activity stack can be instantiated and started
      //   right away
      // - interrupting or cancelling activities for which we have to ensure that
      //   the interruption and cancellation takes place before we instantiate the activity stack
      ActivityImpl topMostActivity = (ActivityImpl) activitiesToInstantiate.get(0);
      boolean isCancelScope = false;
      if (topMostActivity.isCancelScope()) {
        if (activitiesToInstantiate.size() > 1) {
          // this is in BPMN relevant if there is an interrupting event sub process.
          // we have to distinguish between instantiation of the start event and any other activity.
          // instantiation of the start event means interrupting behavior; instantiation
          // of any other task means no interruption.
          ActivityImpl initialActivity = (ActivityImpl) topMostActivity.getProperty(BpmnParse.PROPERTYNAME_INITIAL);
          if (initialActivity == activitiesToInstantiate.get(1)) {
            isCancelScope = true;
          }
        } else {
          isCancelScope = true;
        }
      }

      if (isCancelScope) {
        ScopeImpl scopeToCancel = topMostActivity.getParentScope();
        Set<ExecutionEntity> executionsToCancel = mapping.getExecutions(scopeToCancel);

        if (!executionsToCancel.isEmpty()) {
          if (executionsToCancel.size() > 1) {
            throw new ProcessEngineException("Execution to cancel/interrupt is ambiguous for activity " + topMostActivity);
          }

          ExecutionEntity interruptedExecution = executionsToCancel.iterator().next();

          // this distinguishes between interruption (e.g. event sub process) and
          // cancellation (e.g. boundary event)
          if (scopeToCancel == topMostActivity.getFlowScope()) {
            // perform interruption
            // TODO: the delete reason is a hack
            interruptedExecution.cancelScope("Interrupting event sub process "+ topMostActivity + " fired.");
            interruptedExecution.executeActivities(activitiesToInstantiate,
                startInstruction.getVariables(), startInstruction.getVariablesLocal());
          }
          else {
            // perform cancellation
            // TODO: the delete reason is a hack
            scopeExecution.cancelScope("Cancel scope activity " + topMostActivity + " executed.");
            scopeExecution.executeActivities(activitiesToInstantiate,
                startInstruction.getVariables(), startInstruction.getVariablesLocal());

          }
        } else {
          // if there is nothing to cancel, the activity can simply be instantiated.
          scopeExecution.executeActivitiesConcurrent(activitiesToInstantiate,
              startInstruction.getVariables(), startInstruction.getVariablesLocal());

        }
      }
      else {
        // if the activity is not cancelling/interrupting, it can simply be instantiated as
        // a concurrent child of the scopeExecution
        scopeExecution.executeActivitiesConcurrent(activitiesToInstantiate,
            startInstruction.getVariables(), startInstruction.getVariablesLocal());

      }

    }

    return null;
  }


}
