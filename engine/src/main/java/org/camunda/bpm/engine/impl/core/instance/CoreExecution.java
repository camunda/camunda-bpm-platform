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
package org.camunda.bpm.engine.impl.core.instance;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;

/**
 * Defines the base API for the execution of an activity.
 *
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public abstract class CoreExecution extends AbstractVariableScope implements BaseDelegateExecution {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(CoreExecution.class.getName());

  protected String id;

  /**
   * the business key for this execution
   */
  protected String businessKey;

  // events ///////////////////////////////////////////////////////////////////

  protected String eventName;
  protected CoreModelElement eventSource;
  protected int listenerIndex = 0;
  protected boolean skipCustomListeners;

  // atomic operations ////////////////////////////////////////////////////////

  public <T extends CoreExecution> void performOperation(CoreAtomicOperation<T> operation) {
    if (log.isLoggable(Level.FINEST)) {
      log.finest("AtomicOperation: " + operation + " on " + this);
    }
    operation.execute((T) this);
  }

  @SuppressWarnings("unchecked")
  public <T extends CoreExecution> void performOperationSync(CoreAtomicOperation<T> operation) {
    if (log.isLoggable(Level.FINEST)) {
      log.finest("AtomicOperation: " + operation + " on " + this);
    }
    operation.execute((T) this);
  }

  // event handling ////////////////////////////////////////////////////////

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public CoreModelElement getEventSource() {
    return eventSource;
  }

  public void setEventSource(CoreModelElement eventSource) {
    this.eventSource = eventSource;
  }

  public int getListenerIndex() {
    return listenerIndex;
  }

  public void setListenerIndex(int listenerIndex) {
    this.listenerIndex = listenerIndex;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void invokeListener(DelegateListener listener) throws Exception {
    listener.notify(this);
  }

  // getters / setters /////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

}
