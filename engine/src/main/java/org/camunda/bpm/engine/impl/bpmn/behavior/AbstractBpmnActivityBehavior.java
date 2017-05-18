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

import static org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil.SIGNAL_COMPENSATION_DONE;

import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionHierarchyWalker;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionMappingCollector;
import org.camunda.bpm.engine.impl.tree.ActivityExecutionTuple;
import org.camunda.bpm.engine.impl.tree.OutputVariablesPropagator;
import org.camunda.bpm.engine.impl.tree.ReferenceWalker;
import org.camunda.bpm.engine.impl.tree.TreeVisitor;


/**
 * Denotes an 'activity' in the sense of BPMN 2.0:
 * a parent class for all tasks, subprocess and callActivity.
 *
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class AbstractBpmnActivityBehavior extends FlowNodeActivityBehavior {

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  /**
   * Subclasses that call leave() will first pass through this method, before
   * the regular {@link FlowNodeActivityBehavior#leave(ActivityExecution)} is
   * called.
   */
  @Override
  public void doLeave(ActivityExecution execution) {

    PvmActivity currentActivity = execution.getActivity();
    ActivityImpl compensationHandler = ((ActivityImpl) currentActivity).findCompensationHandler();

    // subscription for compensation event subprocess is already created
    if(compensationHandler != null && !isCompensationEventSubprocess(compensationHandler)) {
      createCompensateEventSubscription(execution, compensationHandler);
    }
    super.doLeave(execution);
  }

  protected boolean isCompensationEventSubprocess(ActivityImpl activity) {
    return activity.isCompensationHandler() && activity.isSubProcessScope() && activity.isTriggeredByEvent();
  }

  protected void createCompensateEventSubscription(ActivityExecution execution, ActivityImpl compensationHandler) {
    // the compensate event subscription is created at subprocess or miBody of the the current activity
    PvmActivity currentActivity = execution.getActivity();
    ActivityExecution scopeExecution = execution.findExecutionForFlowScope(currentActivity.getFlowScope());

    EventSubscriptionEntity.createAndInsert((ExecutionEntity) scopeExecution, EventType.COMPENSATE, compensationHandler);
  }

  protected void propagateExceptionAsError(Exception exception, ActivityExecution execution) throws Exception {
    if (isProcessEngineExceptionWithoutCause(exception) || isTransactionNotActive()) {
      throw exception;
    }
    else {
      propagateError(null, exception.getMessage(),exception, execution);
    }
  }

  /**
   * Takes an {@link ActivityExecution} and an {@link Callable} and wraps
   * the call to the Callable with the proper error propagation. This method
   * also makes sure that exceptions not caught by following activities in the
   * process will be thrown and not propagated.
   *
   * @param execution
   * @param toExecute
   * @throws Exception
   */
  protected void executeWithErrorPropagation(ActivityExecution execution, Callable<Void> toExecute) throws Exception {
    String activityInstanceId = execution.getActivityInstanceId();
    try {
      toExecute.call();
    } catch (Exception ex) {
      if (activityInstanceId.equals(execution.getActivityInstanceId())) {

        try {
          propagateException(execution, ex);
        }
        catch (ErrorPropagationException e) {
          LOG.errorPropagationException(activityInstanceId, e.getCause());
          // re-throw the original exception so that it is logged
          // and set as cause of the failure
          throw ex;
        }

      }
      else {
        throw ex;
      }
    }
  }

  /**
   * Decides how to propagate the exception properly, e.g. as bpmn error or "normal" error.
   * @param execution the current execution
   * @param ex the exception to propagate
   * @throws Exception if no error handler could be found
   */
  protected void propagateException(ActivityExecution execution, Exception ex) throws Exception {
    BpmnError bpmnError = checkIfCauseOfExceptionIsBpmnError(ex);
    if (bpmnError != null) {
      propagateBpmnError(bpmnError, execution);
    } else {
      propagateExceptionAsError(ex, execution);
    }
  }

  /**
   * Searches recursively through the exception to see if the exception itself
   * or one of its causes is a {@link BpmnError}.
   *
   * @param e
   *          the exception to check
   * @return the BpmnError that was the cause of this exception or null if no
   *         BpmnError was found
   */
  protected BpmnError checkIfCauseOfExceptionIsBpmnError(Throwable e) {
    if (e instanceof BpmnError) {
      return (BpmnError) e;
    } else if (e.getCause() == null) {
      return null;
    }
    return checkIfCauseOfExceptionIsBpmnError(e.getCause());
  }

  protected boolean isTransactionNotActive() {
    return !Context.getCommandContext().getTransactionContext().isTransactionActive();
  }

  protected boolean isProcessEngineExceptionWithoutCause(Exception exception) {
    return exception instanceof ProcessEngineException && exception.getCause() == null;
  }

  protected void propagateBpmnError(BpmnError error, ActivityExecution execution) throws Exception {
    propagateError(error.getErrorCode(), error.getMessage(), null, execution);
  }

  protected void propagateError(String errorCode, String errorMessage, Exception origException, ActivityExecution execution) throws Exception {

    ActivityExecutionHierarchyWalker walker = new ActivityExecutionHierarchyWalker(execution);

    final ErrorDeclarationForProcessInstanceFinder errorDeclarationFinder = new ErrorDeclarationForProcessInstanceFinder(origException, errorCode, execution.getActivity());
    ActivityExecutionMappingCollector activityExecutionMappingCollector = new ActivityExecutionMappingCollector(execution);

    walker.addScopePreVisitor(errorDeclarationFinder);
    walker.addExecutionPreVisitor(activityExecutionMappingCollector);
    // map variables to super executions in the hierarchy of called process instances
    walker.addExecutionPreVisitor(new OutputVariablesPropagator());

    try {

      walker.walkUntil(new ReferenceWalker.WalkCondition<ActivityExecutionTuple>() {

        @Override
        public boolean isFulfilled(ActivityExecutionTuple element) {
          return errorDeclarationFinder.getErrorEventDefinition() != null || element == null;
        }
      });

    } catch(Exception e) {
      // separate the exception handling to support a fail-safe error propagation
      throw new ErrorPropagationException(e);
    }

    PvmActivity errorHandlingActivity = errorDeclarationFinder.getErrorHandlerActivity();

    // process the error
    if (errorHandlingActivity == null) {
      if (origException == null) {

        if (Context.getCommandContext().getProcessEngineConfiguration().isEnableExceptionsAfterUnhandledBpmnError()) {
          throw LOG.missingBoundaryCatchEventError(execution.getActivity().getId(), errorCode);
        } else {
          LOG.missingBoundaryCatchEvent(execution.getActivity().getId(), errorCode);
          execution.end(true);
        }
      } else {
        // throw original exception
        throw origException;
      }
    }
    else {

      ErrorEventDefinition errorDefinition = errorDeclarationFinder.getErrorEventDefinition();
      PvmExecutionImpl errorHandlingExecution = activityExecutionMappingCollector.getExecutionForScope(errorHandlingActivity.getEventScope());

      if(errorDefinition.getErrorCodeVariable() != null) {
        errorHandlingExecution.setVariable(errorDefinition.getErrorCodeVariable(), errorCode);
      }
      if(errorDefinition.getErrorMessageVariable() != null) {
        errorHandlingExecution.setVariable(errorDefinition.getErrorMessageVariable(), errorMessage);
      }
      errorHandlingExecution.executeActivity(errorHandlingActivity);
    }
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    if(SIGNAL_COMPENSATION_DONE.equals(signalName)) {
      signalCompensationDone(execution);
    } else {
      super.signal(execution, signalName, signalData);
    }
  }

  protected void signalCompensationDone(ActivityExecution execution) {
    // default behavior is to join compensating executions and propagate the signal if all executions have compensated

    // only wait for non-event-scope executions cause a compensation event subprocess consume the compensation event and
    // do not have to compensate embedded subprocesses (which are still non-event-scope executions)

    if(((PvmExecutionImpl) execution).getNonEventScopeExecutions().isEmpty()) {
      if(execution.getParent() != null) {
        ActivityExecution parent = execution.getParent();
        execution.remove();
        parent.signal(SIGNAL_COMPENSATION_DONE, null);
      }
    } else {
      ((ExecutionEntity)execution).forceUpdate();
    }

  }

  public class ErrorDeclarationForProcessInstanceFinder implements TreeVisitor<PvmScope> {

    protected Exception exception;
    protected String errorCode;
    protected PvmActivity errorHandlerActivity;
    protected ErrorEventDefinition errorEventDefinition;
    protected PvmActivity currentActivity;

    public ErrorDeclarationForProcessInstanceFinder(Exception exception, String errorCode, PvmActivity currentActivity) {
      this.exception = exception;
      this.errorCode = errorCode;
      this.currentActivity = currentActivity;
    }

    @Override
    public void visit(PvmScope scope) {
      List<ErrorEventDefinition> errorEventDefinitions = scope.getProperties().get(BpmnProperties.ERROR_EVENT_DEFINITIONS);
      for (ErrorEventDefinition errorEventDefinition : errorEventDefinitions) {
        PvmActivity activityHandler = scope.getProcessDefinition().findActivity(errorEventDefinition.getHandlerActivityId());
        if ((!isReThrowingErrorEventSubprocess(activityHandler)) && ((exception != null && errorEventDefinition.catchesException(exception))
          || (exception == null && errorEventDefinition.catchesError(errorCode)))) {

          errorHandlerActivity = activityHandler;
          this.errorEventDefinition = errorEventDefinition;
          break;
        }
      }
    }

    protected boolean isReThrowingErrorEventSubprocess(PvmActivity activityHandler) {
      ScopeImpl activityHandlerScope = (ScopeImpl)activityHandler;
      return activityHandlerScope.isAncestorFlowScopeOf((ScopeImpl)currentActivity);
    }

    public PvmActivity getErrorHandlerActivity() {
      return errorHandlerActivity;
    }

    public ErrorEventDefinition getErrorEventDefinition() {
      return errorEventDefinition;
    }
  }

  protected class ErrorPropagationException extends Exception {

    private static final long serialVersionUID = 1L;

    public ErrorPropagationException(Throwable cause) {
      super(cause);
    }
  }

}
