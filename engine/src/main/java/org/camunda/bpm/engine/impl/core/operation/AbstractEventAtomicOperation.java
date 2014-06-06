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

package org.camunda.bpm.engine.impl.core.operation;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.model.CoreActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;

import java.util.List;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractEventAtomicOperation<T extends CoreExecution> implements CoreAtomicOperation<T> {

  public boolean isAsync(T execution) {
    return false;
  }

  public void execute(T instance) {

    CoreActivity scope = getScope(instance);
    List<DelegateListener<? extends BaseDelegateExecution>> listeners = scope.getListeners(getEventName());
    int listenerIndex = instance.getListenerIndex();

    if(listenerIndex == 0) {
      instance = eventNotificationsStarted(instance);
    }

    if(!isSkipNotifyListeners(instance)) {

      if (listeners.size()>listenerIndex) {
        instance.setEventName(getEventName());
        instance.setEventSource(scope);
        DelegateListener<? extends BaseDelegateExecution> listener = listeners.get(listenerIndex);
        try {
          instance.invokeListener(listener);
        } catch (RuntimeException e) {
          throw e;
        } catch (Exception e) {
          throw new PvmException("couldn't execute event listener : "+e.getMessage(), e);
        }
        instance.setListenerIndex(listenerIndex+1);
        instance.performOperationSync(this);

      } else {
        instance.setListenerIndex(0);
        instance.setEventName(null);
        instance.setEventSource(null);

        eventNotificationsCompleted(instance);
      }

    } else {
      eventNotificationsCompleted(instance);

    }
  }

  protected boolean isSkipNotifyListeners(T instance) {
    return false;
  }

  protected T eventNotificationsStarted(T instance) {
    // do nothing
    return instance;
  }

  protected abstract CoreActivity getScope(T instance);
  protected abstract String getEventName();
  protected abstract void eventNotificationsCompleted(T instance);
}
