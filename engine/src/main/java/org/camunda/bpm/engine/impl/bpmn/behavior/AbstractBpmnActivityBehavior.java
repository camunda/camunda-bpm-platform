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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.SubProcessActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.tree.Collector;
import org.camunda.bpm.engine.impl.tree.TreeWalker;
import org.camunda.bpm.engine.impl.tree.TreeWalker.WalkCondition;




/**
 * Denotes an 'activity' in the sense of BPMN 2.0:
 * a parent class for all tasks, subprocess and callActivity.
 *
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class AbstractBpmnActivityBehavior extends FlowNodeActivityBehavior {

  private static final Logger LOG = Logger.getLogger(AbstractBpmnActivityBehavior.class.getName());

  /**
   * Subclasses that call leave() will first pass through this method, before
   * the regular {@link FlowNodeActivityBehavior#leave(ActivityExecution)} is
   * called.
   */
  protected void leave(ActivityExecution execution) {
    PvmActivity currentActivity = execution.getActivity();
    ActivityImpl compensationHandler = getCompensationHandler(currentActivity);
    if(compensationHandler != null) {
      createCompensateEventSubscription(execution, compensationHandler);
    }
    super.leave(execution);
  }

  protected ActivityImpl getCompensationHandler(PvmActivity activity) {
    String compensationHandlerId = (String) activity.getProperty(BpmnParse.PROPERTYNAME_COMPENSATION_HANDLER_ID);
    if(compensationHandlerId != null) {
      return (ActivityImpl) activity.getProcessDefinition().findActivity(compensationHandlerId);
    }
    else {
      return null;
    }
  }

  protected void createCompensateEventSubscription(ActivityExecution execution, ActivityImpl compensationHandler) {

    PvmActivity currentActivity = execution.getActivity();
    PvmScope levelOfSubprocessScope = currentActivity.getLevelOfSubprocessScope();

    // the compensate event subscription is created "at the level of subprocess" of the the current activity.
    ActivityExecution levelOfSubprocessScopeExecution = execution.findExecutionForFlowScope(levelOfSubprocessScope);

    CompensateEventSubscriptionEntity.createAndInsert((ExecutionEntity) levelOfSubprocessScopeExecution, compensationHandler);
  }

  protected void propagateExceptionAsError(Exception exception, ActivityExecution execution) throws Exception {
    if (exception instanceof ProcessEngineException && exception.getCause() == null) {
      throw exception;
    } else {
      propagateError(null, exception, execution);
    }
  }

  protected void propagateBpmnError(BpmnError error, ActivityExecution execution) throws Exception {
    propagateError(error.getErrorCode(), null, execution);
  }

  protected void propagateError(String errorCode, Exception origException, ActivityExecution execution) throws Exception {

    // get the scope activity or process definition for the current execution
    ScopeImpl scope = getCurrentFlowScope(execution);

    // make sure we start with the scope execution for the current scope
    PvmExecutionImpl scopeExecution = (PvmExecutionImpl) (execution.isScope() ? execution : execution.getParent());

    // walk the tree of parent scope executions and activities and search a scope in both trees which catches the error
    ExecutionScopeHierarchyWalker scopeHierarchyWalker = new ExecutionScopeHierarchyWalker(scopeExecution, true);

    // collectors
    ErrorDeclarationFinder errorDeclarationFinder = new ErrorDeclarationFinder(scope, errorCode, origException, (PvmExecutionImpl) execution);
    ProcessInstanceCollector processInstanceCollector = new ProcessInstanceCollector();

    // walk
    scopeHierarchyWalker.addPreCollector(errorDeclarationFinder);
    scopeHierarchyWalker.addPreCollector(processInstanceCollector);
    scopeHierarchyWalker.walkWhile(errorDeclarationFinder);

    PvmActivity errorHandlingActivity = errorDeclarationFinder.getErrorHandlerActivity();
    ErrorEventDefinition errorDefinition = errorDeclarationFinder.getErrorEventDefinition();
    PvmExecutionImpl errorHandlingExecution = null;
    if(errorHandlingActivity != null) {
      // Why is this necessary (or: why can we not just use the last execution the ExecutionScopeHierarchyWalker looked at?)
      // => legacy behavior: the execution hierarchy may be out of sync with the scope hierarchy. findExecutionForFlowScope()
      // will return the correct scope execution even for trees which are out of sync
      // TODO: maybe all of this can be simplified? However: in order to handle the "out of sync tree case", we always need to create the complete
      // tree scope for the process instance which handles the error. Otherwise we cannot detect out of sync trees.
      errorHandlingExecution = errorDeclarationFinder.getLastLeafExecution().findExecutionForFlowScope(errorHandlingActivity.getEventScope());
    }

    // map variables to super executions in the hierarchy of called process instances
    for (PvmExecutionImpl processInstance : processInstanceCollector.getProcessInstanceHierarchy()) {
      PvmExecutionImpl superExecution = processInstance.getSuperExecution();
      ActivityImpl activity = ((PvmExecutionImpl)superExecution).getActivity();
      SubProcessActivityBehavior subProcessActivityBehavior = (SubProcessActivityBehavior) activity.getActivityBehavior();
      subProcessActivityBehavior.completing(superExecution, processInstance);
    }

    // process the error
    if (errorHandlingExecution == null) {
      if (origException == null) {
        LOG.info(execution.getActivity().getId() + " throws error event with errorCode '"
            + errorCode + "', but no catching boundary event was defined. "
            +   "Execution is ended (none end event semantics).");
        execution.end(true);
      } else {
        // throw original exception
        throw origException;
      }
    }
    else {
      if(errorDefinition.getErrorCodeVariable() != null){
        errorHandlingExecution.setVariable(errorDefinition.getErrorCodeVariable(), errorCode);
      }
      errorHandlingExecution.executeActivity(errorHandlingActivity);
    }
  }

  /**
   * Assumption: execution is executing a transition or an activity.
   *
   * @return the scope for the transition or activity the execution is currently executing
   */
  protected ScopeImpl getCurrentFlowScope(ActivityExecution execution) {
    ScopeImpl scope = null;
    if(execution.getTransition() != null) {
      // error may be thrown from a sequence flow listener(?)
      scope = execution.getTransition().getDestination().getFlowScope();
    }
    else {
      scope = (ScopeImpl) execution.getActivity();
    }

    // the execution may currently not be executing a scope activity
    scope = scope.isScope() ? scope : scope.getFlowScope();

    return scope;
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    if("compensationDone".equals(signalName)) {
      signalCompensationDone(execution, signalData);
    } else {
      super.signal(execution, signalName, signalData);
    }
  }

  protected void signalCompensationDone(ActivityExecution execution, Object signalData) {
    // default behavior is to join compensating executions and propagate the signal if all executions
    // have compensated

    // join compensating executions
    if(execution.getExecutions().isEmpty()) {
      if(execution.getParent() != null) {
        ActivityExecution parent = execution.getParent();
        execution.remove();
        parent.signal("compensationDone", signalData);
      }
    } else {
      ((ExecutionEntity)execution).forceUpdate();
    }

  }

  /**
   * Walks the execution tree hierarchy skipping all non-scope executions.
   */
  public static class ExecutionScopeHierarchyWalker extends TreeWalker<PvmExecutionImpl> {

    protected boolean considerSuperProcessInstances;

    public ExecutionScopeHierarchyWalker(PvmExecutionImpl initialElement, boolean considerSuperProcessInstances) {
      super(initialElement);
      this.considerSuperProcessInstances = considerSuperProcessInstances;
    }

    protected PvmExecutionImpl nextElement() {
      return currentElement.getParentScopeExecution(considerSuperProcessInstances);
    }

  }

  public static class ErrorDeclarationFinder implements Collector<PvmExecutionImpl>, WalkCondition<PvmExecutionImpl> {

    protected ScopeImpl currentScope;
    protected String errorCode;
    protected Exception exception;
    protected ErrorEventDefinition errorEventDefinition;
    protected PvmActivity errorHandlerActivity;
    protected PvmExecutionImpl lastLeafExecution;

    public ErrorDeclarationFinder(ScopeImpl currentScope, String errorCode, Exception exception, PvmExecutionImpl lastLeafExecution) {
      this.currentScope = currentScope;
      this.errorCode = errorCode;
      this.exception = exception;
      this.lastLeafExecution = lastLeafExecution;
    }

    public boolean isFulfilled(PvmExecutionImpl element) {
      if (currentScope == null) {
        return true;
      }
      else {
        List<ErrorEventDefinition> errorEventDefinitions = (List) currentScope.getProperty(BpmnParse.PROPERTYNAME_ERROR_EVENT_DEFINITIONS);
        if(errorEventDefinitions != null) {
          for (ErrorEventDefinition errorEventDefinition : errorEventDefinitions) {
            if ((exception != null && errorEventDefinition.catchesException(exception)) ||
                (exception == null && errorEventDefinition.catchesError(errorCode))) {
              errorHandlerActivity = currentScope.getProcessDefinition().findActivity(errorEventDefinition.getHandlerActivityId());
              this.errorEventDefinition = errorEventDefinition;
              return true;
            }
          }
        }
        return false;
      }

    }

    public void collect(PvmExecutionImpl obj) {
      currentScope = currentScope.getFlowScope();

      // if process definition was already reached, go one process definition up
      if (currentScope == null) {
        PvmExecutionImpl superExecution = obj.getSuperExecution();

        if (superExecution != null) {
          currentScope = superExecution.getActivity();
          lastLeafExecution = superExecution;
        }
      }

      if (currentScope != null) {
        // the execution may currently not be executing a scope activity
        currentScope = currentScope.isScope() ? currentScope : currentScope.getFlowScope();
      }
    }

    public PvmActivity getErrorHandlerActivity() {
      return errorHandlerActivity;
    }

    public ErrorEventDefinition getErrorEventDefinition() {
      return errorEventDefinition;
    }

    public PvmExecutionImpl getLastLeafExecution() {
      return lastLeafExecution;
    }

  }

  /**
   * Finds all process instance executions that have a super execution (i.e. called process instances)
   */
  public static class ProcessInstanceCollector implements Collector<PvmExecutionImpl> {

    protected List<PvmExecutionImpl> processInstanceHierarchy = new ArrayList<PvmExecutionImpl>();

    public void collect(PvmExecutionImpl obj) {
      if (obj.getSuperExecution() != null) {
        processInstanceHierarchy.add(obj);
      }
    }

    public List<PvmExecutionImpl> getProcessInstanceHierarchy() {
      return processInstanceHierarchy;
    }

  }

}
