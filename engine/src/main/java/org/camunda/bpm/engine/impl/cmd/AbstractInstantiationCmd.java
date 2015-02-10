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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ActivityExecutionMapping;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityStartBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.tree.ActivityStackCollector;
import org.camunda.bpm.engine.impl.tree.FlowScopeWalker;
import org.camunda.bpm.engine.impl.tree.TreeWalker.WalkCondition;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class AbstractInstantiationCmd extends AbstractProcessInstanceModificationCommand {

  protected VariableMap variables;
  protected VariableMap variablesLocal;
  protected String ancestorActivityInstanceId;

  public AbstractInstantiationCmd(String processInstanceId, String ancestorActivityInstanceId) {
    super(processInstanceId);
    this.ancestorActivityInstanceId = ancestorActivityInstanceId;
    this.variables = new VariableMapImpl();
    this.variablesLocal = new VariableMapImpl();
  }

  public void addVariable(String name, Object value) {
    this.variables.put(name, value);
  }

  public void addVariableLocal(String name, Object value) {
    this.variablesLocal.put(name, value);
  }

  public void addVariables(Map<String, Object> variables) {
    this.variables.putAll(variables);
  }

  public void addVariablesLocal(Map<String, Object> variables) {
    this.variablesLocal.putAll(variables);
  }

  public VariableMap getVariables() {
    return variables;
  }

  public VariableMap getVariablesLocal() {
    return variablesLocal;
  }

  public Void execute(final CommandContext commandContext) {
    ExecutionEntity processInstance = commandContext.getExecutionManager().findExecutionById(processInstanceId);
    final ProcessDefinitionImpl processDefinition = processInstance.getProcessDefinition();

    CoreModelElement elementToInstantiate = getTargetElement(processDefinition);

    EnsureUtil.ensureNotNull("element", elementToInstantiate);

    // rebuild the mapping because the execution tree changes with every iteration
    final ActivityExecutionMapping mapping = new ActivityExecutionMapping(commandContext, processInstanceId);

    // before instantiating an activity, two things have to be determined:
    //
    // activityStack:
    // For the activity to instantiate, we build a stack of parent flow scopes
    // for which no executions exist yet and that have to be instantiated
    //
    // scopeExecution:
    // This is typically the execution under which a new sub tree has to be created.
    // if an explicit ancestor activity instance is set:
    //   - this is the scope execution for that ancestor activity instance
    //   - throws exception if that scope execution is not in the parent hierarchy
    //     of the activity to be started
    // if no explicit ancestor activity instance is set:
    //   - this is the execution of the first parent/ancestor flow scope that has an execution
    //   - throws an exception if there is more than one such execution

    ScopeImpl flowScope = getTargetFlowScope(processDefinition);

    // prepare to walk up the flow scope hierarchy and collect the flow scope activities
    ActivityStackCollector stackCollector = new ActivityStackCollector();
    FlowScopeWalker walker = new FlowScopeWalker(flowScope);
    walker.addPreCollector(stackCollector);

    ExecutionEntity scopeExecution = null;

    // if no explicit ancestor activity instance is set
    if (ancestorActivityInstanceId == null) {
      // walk until a scope is reached for which executions exist
      walker.walkWhile(new WalkCondition<ScopeImpl>() {
        public boolean isFulfilled(ScopeImpl element) {
          return !mapping.getExecutions(element).isEmpty() || element == processDefinition;
        }
      });

      Set<ExecutionEntity> flowScopeExecutions = mapping.getExecutions(walker.getCurrentElement());

      if (flowScopeExecutions.size() > 1) {
        throw new ProcessEngineException("Ancestor activity execution is ambiguous for activity " + flowScope);
      }

      scopeExecution = flowScopeExecutions.iterator().next();
    }
    else {
      ActivityInstance tree = commandContext.runWithoutAuthentication(new Callable<ActivityInstance>() {
        public ActivityInstance call() throws Exception {
          return new GetActivityInstanceCmd(processInstanceId).execute(commandContext);
        }
      });

      ActivityInstance ancestorInstance = findActivityInstance(tree, ancestorActivityInstanceId);

      // determine ancestor activity scope execution and activity
      final ExecutionEntity ancestorScopeExecution = getScopeExecutionForActivityInstance(processInstance,
            mapping, ancestorInstance);
      final PvmScope ancestorScope = getScopeForActivityInstance(processDefinition, ancestorInstance);

      // walk until the scope of the ancestor scope execution is reached
      walker.walkWhile(new WalkCondition<ScopeImpl>() {
        public boolean isFulfilled(ScopeImpl element) {
          return (
              mapping.getExecutions(element).contains(ancestorScopeExecution)
              && element == ancestorScope)
            || element == processDefinition;
        }
      });

      Set<ExecutionEntity> flowScopeExecutions = mapping.getExecutions(walker.getCurrentElement());

      if (!flowScopeExecutions.contains(ancestorScopeExecution)) {
        throw new ProcessEngineException("Could not find scope execution for " + ancestorActivityInstanceId +
            " in parent hierarchy of flow element " + elementToInstantiate);
      }

      scopeExecution = ancestorScopeExecution;
    }

    List<PvmActivity> activitiesToInstantiate = stackCollector.getActivityStack();
    Collections.reverse(activitiesToInstantiate);

    // We have to make a distinction between
    // - "regular" activities for which the activity stack can be instantiated and started
    //   right away
    // - interrupting or cancelling activities for which we have to ensure that
    //   the interruption and cancellation takes place before we instantiate the activity stack
    ActivityImpl topMostActivity = null;
    if (!activitiesToInstantiate.isEmpty()) {
      topMostActivity = (ActivityImpl) activitiesToInstantiate.get(0);
    }
    else if (ActivityImpl.class.isAssignableFrom(elementToInstantiate.getClass())) {
      topMostActivity = (ActivityImpl) elementToInstantiate;
    }

    ActivityStartBehavior startBehavior = ActivityStartBehavior.CONCURRENT_IN_FLOW_SCOPE;
    if (topMostActivity != null) {
      startBehavior = topMostActivity.getActivityStartBehavior();

      if (!activitiesToInstantiate.isEmpty()) {
        // this is in BPMN relevant if there is an interrupting event sub process.
        // we have to distinguish between instantiation of the start event and any other activity.
        // instantiation of the start event means interrupting behavior; instantiation
        // of any other task means no interruption.
        PvmActivity initialActivity = (PvmActivity) topMostActivity.getProperty(BpmnParse.PROPERTYNAME_INITIAL);
        PvmActivity secondTopMostActivity = null;
        if (activitiesToInstantiate.size() > 1) {
          secondTopMostActivity = (PvmActivity) activitiesToInstantiate.get(1);
        }
        else if (ActivityImpl.class.isAssignableFrom(elementToInstantiate.getClass())) {
          secondTopMostActivity = (PvmActivity) elementToInstantiate;
        }

        if (initialActivity != secondTopMostActivity) {
          startBehavior = ActivityStartBehavior.CONCURRENT_IN_FLOW_SCOPE;
        }
      }
    }

    switch (startBehavior) {
      case CANCEL_EVENT_SCOPE:
        {
          ScopeImpl scopeToCancel = topMostActivity.getEventScope();
          ExecutionEntity executionToCancel = getSingleExecutionForScope(mapping, scopeToCancel);
          if (executionToCancel != null) {
            executionToCancel.deleteCascade("Cancelling activity " + topMostActivity + " executed.", skipCustomListeners, skipIoMappings);
            instantiate(executionToCancel.getParent(), activitiesToInstantiate, elementToInstantiate);
          }
          else {
            ExecutionEntity flowScopeExecution = getSingleExecutionForScope(mapping, topMostActivity.getFlowScope());
            instantiateConcurrent(flowScopeExecution, activitiesToInstantiate, elementToInstantiate);
          }
          break;
        }
      case INTERRUPT_EVENT_SCOPE:
        {
          ScopeImpl scopeToCancel = topMostActivity.getEventScope();
          ExecutionEntity executionToCancel = getSingleExecutionForScope(mapping, scopeToCancel);
          executionToCancel.interrupt("Interrupting activity " + topMostActivity + " executed.", skipCustomListeners, skipIoMappings);
          executionToCancel.setActivity(null);
          executionToCancel.leaveActivityInstance();
          instantiate(executionToCancel, activitiesToInstantiate, elementToInstantiate);
          break;
        }
      case INTERRUPT_FLOW_SCOPE:
        {
          ScopeImpl scopeToCancel = topMostActivity.getFlowScope();
          ExecutionEntity executionToCancel = getSingleExecutionForScope(mapping, scopeToCancel);
          executionToCancel.interrupt("Interrupting activity " + topMostActivity + " executed.", skipCustomListeners, skipIoMappings);
          executionToCancel.setActivity(null);
          executionToCancel.leaveActivityInstance();
          instantiate(executionToCancel, activitiesToInstantiate, elementToInstantiate);
          break;
        }
      default:
        {
          if (scopeExecution.getExecutions().isEmpty() && scopeExecution.getActivity() == null) {
            // reuse the scope execution
            instantiate(scopeExecution, activitiesToInstantiate, elementToInstantiate);
          } else {
            // if the activity is not cancelling/interrupting, it can simply be instantiated as
            // a concurrent child of the scopeExecution
            instantiateConcurrent(scopeExecution, activitiesToInstantiate, elementToInstantiate);
          }
          break;
        }
    }

    return null;
  }

  protected ExecutionEntity getSingleExecutionForScope(ActivityExecutionMapping mapping, ScopeImpl scope) {
    Set<ExecutionEntity> executions = mapping.getExecutions(scope);

    if (!executions.isEmpty()) {
      if (executions.size() > 1) {
        throw new ProcessEngineException("Executions for activity " + scope + " ambiguous");
      }

      return executions.iterator().next();
    }
    else {
      return null;
    }
  }

  protected boolean isConcurrentStart(ActivityStartBehavior startBehavior) {
    return startBehavior == ActivityStartBehavior.DEFAULT
        || startBehavior == ActivityStartBehavior.CONCURRENT_IN_FLOW_SCOPE;
  }

  protected void instantiate(ExecutionEntity ancestorScopeExecution, List<PvmActivity> parentFlowScopes, CoreModelElement targetElement) {
    if (PvmTransition.class.isAssignableFrom(targetElement.getClass())) {
      ancestorScopeExecution.executeActivities(parentFlowScopes, null, (PvmTransition) targetElement, variables, variablesLocal,
          skipCustomListeners, skipIoMappings);
    }
    else if (PvmActivity.class.isAssignableFrom(targetElement.getClass())) {
      ancestorScopeExecution.executeActivities(parentFlowScopes, (PvmActivity) targetElement, null, variables, variablesLocal,
          skipCustomListeners, skipIoMappings);

    }
    else {
      throw new ProcessEngineException("Cannot instantiate element " + targetElement);
    }
  }


  protected void instantiateConcurrent(ExecutionEntity ancestorScopeExecution, List<PvmActivity> parentFlowScopes, CoreModelElement targetElement) {
    if (PvmTransition.class.isAssignableFrom(targetElement.getClass())) {
      ancestorScopeExecution.executeActivitiesConcurrent(parentFlowScopes, null, (PvmTransition) targetElement, variables,
          variablesLocal, skipCustomListeners, skipIoMappings);
    }
    else if (PvmActivity.class.isAssignableFrom(targetElement.getClass())) {
      ancestorScopeExecution.executeActivitiesConcurrent(parentFlowScopes, (PvmActivity) targetElement, null, variables,
          variablesLocal, skipCustomListeners, skipIoMappings);

    }
    else {
      throw new ProcessEngineException("Cannot instantiate element " + targetElement);
    }
  }

  protected abstract ScopeImpl getTargetFlowScope(ProcessDefinitionImpl processDefinition);

  protected abstract CoreModelElement getTargetElement(ProcessDefinitionImpl processDefinition);

}
