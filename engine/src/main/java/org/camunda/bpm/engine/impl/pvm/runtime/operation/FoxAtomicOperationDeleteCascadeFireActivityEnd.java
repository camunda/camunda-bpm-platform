/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.List;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


public class FoxAtomicOperationDeleteCascadeFireActivityEnd extends PvmAtomicOperationDeleteCascadeFireActivityEnd {

  @Override
  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {
    PvmActivity activity = execution.getActivity();
    if ( (execution.isScope())
            && (activity!=null)
            && (!activity.isScope())
          )  {
      execution.setActivity((PvmActivity) activity.getFlowScope());
      execution.performOperation(this);

    } else {
      if (execution.isScope()) {
        execution.destroy();
      }

      execution.remove();
    }
  }

  @Override
  public void execute(PvmExecutionImpl execution) {
    ScopeImpl scope = getScope(execution);
    int executionListenerIndex = execution.getListenerIndex();
    List<DelegateListener<? extends BaseDelegateExecution>> executionListeners = scope.getListeners(getEventName());
    for (DelegateListener<? extends BaseDelegateExecution> listener : executionListeners) {
      execution.setEventName(getEventName());
      execution.setEventSource(scope);
      try {
        execution.invokeListener(listener);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new PvmException("couldn't execute event listener : "+e.getMessage(), e);
      }
      executionListenerIndex += 1;
      execution.setListenerIndex(executionListenerIndex);
    }
    execution.setListenerIndex(0);
    execution.setEventName(null);
    execution.setEventSource(null);

    eventNotificationsCompleted(execution);
  }


}
