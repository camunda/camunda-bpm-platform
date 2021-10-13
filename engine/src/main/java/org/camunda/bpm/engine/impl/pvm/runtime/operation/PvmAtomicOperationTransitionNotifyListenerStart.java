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
package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.ScopeInstantiationContext;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class PvmAtomicOperationTransitionNotifyListenerStart extends PvmAtomicOperationActivityInstanceStart {

  protected ScopeImpl getScope(PvmExecutionImpl execution) {
    return execution.getActivity();
  }

  protected String getEventName() {
    return ExecutionListener.EVENTNAME_START;
  }

  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {

    super.eventNotificationsCompleted(execution);

    TransitionImpl transition = execution.getTransition();
    PvmActivity destination;
    if (transition == null) { // this is null after async cont. -> transition is not stored in execution
      destination = execution.getActivity();
    } else {
      destination = transition.getDestination();
    }
    execution.setTransition(null);
    execution.setActivity(destination);

    if (execution.isProcessInstanceStarting()) {
      // only call this method if we are currently in the starting phase;
      // if not, this may make an unnecessary request to fetch the process
      // instance from the database
      execution.setProcessInstanceStarting(false);
    }

    execution.dispatchDelayedEventsAndPerformOperation(ACTIVITY_EXECUTE);
  }

  public String getCanonicalName() {
    return "transition-notifiy-listener-start";
  }

  @Override
  public boolean shouldHandleFailureAsBpmnError() {
    return true;
  }
}
