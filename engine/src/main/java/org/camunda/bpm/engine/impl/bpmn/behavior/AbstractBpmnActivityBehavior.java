/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.bpmn.behavior;

import static org.camunda.bpm.engine.impl.bpmn.helper.CompensationUtil.SIGNAL_COMPENSATION_DONE;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnExceptionHandler;
import org.camunda.bpm.engine.impl.bpmn.helper.ErrorPropagationException;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


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
          BpmnExceptionHandler.propagateException(execution, ex);
        }
        catch (ErrorPropagationException e) {
          // exception has been logged by thrower
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

}
