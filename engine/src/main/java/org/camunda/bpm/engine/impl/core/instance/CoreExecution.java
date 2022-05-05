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
package org.camunda.bpm.engine.impl.core.instance;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.impl.core.CoreLogger;
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

  private final static CoreLogger LOG = CoreLogger.CORE_LOGGER;

  protected String id;

  /**
   * the business key for this execution
   */
  protected String businessKey;
  protected String businessKeyWithoutCascade;

  protected String tenantId;

  // events ///////////////////////////////////////////////////////////////////

  protected String eventName;
  protected CoreModelElement eventSource;
  protected int listenerIndex = 0;
  protected boolean skipCustomListeners;
  protected boolean skipIoMapping;
  protected boolean skipSubprocesses;

  // atomic operations ////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public <T extends CoreExecution> void performOperation(CoreAtomicOperation<T> operation) {
    LOG.debugPerformingAtomicOperation(operation, this);
    operation.execute((T) this);
  }

  @SuppressWarnings("unchecked")
  public <T extends CoreExecution> void performOperationSync(CoreAtomicOperation<T> operation) {
    LOG.debugPerformingAtomicOperation(operation, this);
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

  public boolean hasFailedOnEndListeners() {
    return false;
  }

  // getters / setters /////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBusinessKeyWithoutCascade() {
    return businessKeyWithoutCascade;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
    this.businessKeyWithoutCascade = businessKey;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMapping;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMapping = skipIoMappings;
  }

  public boolean isSkipSubprocesses() {
    return skipSubprocesses;
  }

  public void setSkipSubprocesseses(boolean skipSubprocesses) {
    this.skipSubprocesses = skipSubprocesses;
  }

}
