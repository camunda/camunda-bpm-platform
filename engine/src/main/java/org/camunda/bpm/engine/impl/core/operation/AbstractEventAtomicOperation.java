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
package org.camunda.bpm.engine.impl.core.operation;

import java.util.List;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.pvm.PvmException;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractEventAtomicOperation<T extends CoreExecution> implements CoreAtomicOperation<T> {

  @Override
  public boolean isAsync(T execution) {
    return false;
  }

  @Override
  public void execute(T execution) {
    CoreModelElement scope = getScope(execution);
    List<DelegateListener<? extends BaseDelegateExecution>> listeners = execution.hasFailedOnEndListeners()
        ? getBuiltinListeners(scope)
        : getListeners(scope, execution);
    int listenerIndex = execution.getListenerIndex();

    if(listenerIndex == 0) {
      execution = eventNotificationsStarted(execution);
    }

    if(!isSkipNotifyListeners(execution)) {

      if (listeners.size()>listenerIndex) {
        execution.setEventName(getEventName());
        execution.setEventSource(scope);
        DelegateListener<? extends BaseDelegateExecution> listener = listeners.get(listenerIndex);
        execution.setListenerIndex(listenerIndex+1);

        try {
          execution.invokeListener(listener);
        } catch (Exception ex) {
          eventNotificationsFailed(execution, ex);
          // do not continue listener invocation once a listener has failed
          return;
        }

        execution.performOperationSync(this);
      } else {
        resetListeners(execution);

        eventNotificationsCompleted(execution);
      }

    } else {
      eventNotificationsCompleted(execution);

    }
  }

  protected void resetListeners(T execution) {
    execution.setListenerIndex(0);
    execution.setEventName(null);
    execution.setEventSource(null);
  }

  protected List<DelegateListener<? extends BaseDelegateExecution>> getListeners(CoreModelElement scope, T execution) {
    if(execution.isSkipCustomListeners()) {
      return getBuiltinListeners(scope);
    } else {
      return scope.getListeners(getEventName());
    }
  }

  protected List<DelegateListener<? extends BaseDelegateExecution>> getBuiltinListeners(CoreModelElement scope) {
    return scope.getBuiltInListeners(getEventName());
  }

  protected boolean isSkipNotifyListeners(T execution) {
    return false;
  }

  protected T eventNotificationsStarted(T execution) {
    // do nothing
    return execution;
  }

  protected abstract CoreModelElement getScope(T execution);
  protected abstract String getEventName();
  protected abstract void eventNotificationsCompleted(T execution);

  protected void eventNotificationsFailed(T execution, Exception exception) {
    if (exception instanceof RuntimeException) {
      throw (RuntimeException) exception;
    } else {
      throw new PvmException("couldn't execute event listener : " + exception.getMessage(), exception);
    }
  }
}
