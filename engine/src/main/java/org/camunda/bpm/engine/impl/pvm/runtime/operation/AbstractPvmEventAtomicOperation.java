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

import org.camunda.bpm.engine.impl.bpmn.helper.BpmnExceptionHandler;
import org.camunda.bpm.engine.impl.bpmn.helper.ErrorPropagationException;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.core.operation.AbstractEventAtomicOperation;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public abstract class AbstractPvmEventAtomicOperation extends AbstractEventAtomicOperation<PvmExecutionImpl> implements PvmAtomicOperation {

  protected abstract CoreModelElement getScope(PvmExecutionImpl execution);

  public boolean isAsyncCapable() {
    return false;
  }

  @Override
  protected void eventNotificationsFailed(PvmExecutionImpl execution, Exception exception) {

    if (shouldHandleFailureAsBpmnError()) {
      ActivityExecution activityExecution = (ActivityExecution) execution;
      try {
        resetListeners(execution);
        BpmnExceptionHandler.propagateException(activityExecution, exception);
      } catch (ErrorPropagationException e) {
        // exception has been logged by thrower
        // re-throw the original exception so that it is logged
        // and set as cause of the failure
        super.eventNotificationsFailed(execution, exception);
      } catch (Exception e)
      {
        super.eventNotificationsFailed(execution, e);
      }
    } else {
      super.eventNotificationsFailed(execution, exception);
    }
  }

  public boolean shouldHandleFailureAsBpmnError() {
    return false;
  }
}
