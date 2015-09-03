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
package org.camunda.bpm.engine.impl.interceptor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.operation.PvmAtomicOperation;

/**
 * An invocation of an atomic operation
 *
 * @author Daniel Meyer
 *
 */
public class AtomicOperationInvocation {

  private static Logger log = Logger.getLogger(CommandContext.class.getName());

  protected AtomicOperation operation;

  protected ExecutionEntity execution;

  protected boolean performAsync;

  // for logging
  protected String applicationContextName = null;
  protected String activityId = null;

  public AtomicOperationInvocation(AtomicOperation operation, ExecutionEntity execution, boolean performAsync) {
    init(operation, execution, performAsync);
  }

  protected void init(AtomicOperation operation, ExecutionEntity execution, boolean performAsync) {
    this.operation = operation;
    this.execution = execution;
    this.performAsync = performAsync;
  }

  public void execute(BpmnStackTrace stackTrace) {

    if(operation != PvmAtomicOperation.ACTIVITY_START_CANCEL_SCOPE
        && operation != PvmAtomicOperation.ACTIVITY_START_INTERRUPT_SCOPE) {
      // execution might be replaced in the meantime:
      ExecutionEntity replacedBy = execution.getReplacedBy();
      if(replacedBy != null) {
        execution = replacedBy;
      }
    }

    // execution might have ended in the meanwhile
    if(execution.isEnded() &&
        (operation == PvmAtomicOperation.TRANSITION_NOTIFY_LISTENER_TAKE
      || operation == PvmAtomicOperation.ACTIVITY_START_CREATE_SCOPE)) {
      return;
    }

    ProcessApplicationReference currentPa = Context.getCurrentProcessApplication();
    if(currentPa != null) {
      applicationContextName = currentPa.getName();
    }
    activityId = execution.getActivityId();
    stackTrace.add(this);

    try {
      Context.setExecutionContext(execution);
      if(!performAsync) {
        if (log.isLoggable(Level.FINEST)) {
          log.finest("AtomicOperation: " + operation + " on " + execution);
        }
        operation.execute(execution);
      }
      else {
        execution.scheduleAtomicOperationAsync(operation);
      }
    } finally {
      Context.removeExecutionContext();
    }
  }

  // getters / setters ////////////////////////////////////

  public AtomicOperation getOperation() {
    return operation;
  }

  public ExecutionEntity getExecution() {
    return execution;
  }

  public boolean isPerformAsync() {
    return performAsync;
  }

  public String getApplicationContextName() {
    return applicationContextName;
  }

  public String getActivityId() {
    return activityId;
  }

}
