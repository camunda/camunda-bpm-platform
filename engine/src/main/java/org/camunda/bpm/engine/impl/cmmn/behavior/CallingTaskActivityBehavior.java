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
package org.camunda.bpm.engine.impl.cmmn.behavior;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;

/**
 * @author Roman Smirnov
 *
 */
public abstract class CallingTaskActivityBehavior extends TaskActivityBehavior {

  protected static final CmmnBehaviorLogger LOG = ProcessEngineLogger.CMNN_BEHAVIOR_LOGGER;

  protected BaseCallableElement callableElement;

  public void onManualCompletion(CmmnActivityExecution execution) {
    // Throw always an exception!
    // It should not be possible to complete a calling
    // task manually. If the called instance has
    // been completed, the associated task will
    // be notified to complete automatically.
    String id = execution.getId();
    throw LOG.forbiddenManualCompletitionException("complete", id, getTypeName());
  }

  public BaseCallableElement getCallableElement() {
    return callableElement;
  }

  public void setCallableElement(BaseCallableElement callableElement) {
    this.callableElement = callableElement;
  }

  protected String getDefinitionKey(CmmnActivityExecution execution) {
    CmmnExecution caseExecution = (CmmnExecution) execution;
    return getCallableElement().getDefinitionKey(caseExecution);
  }

  protected Integer getVersion(CmmnActivityExecution execution) {
    CmmnExecution caseExecution = (CmmnExecution) execution;
    return getCallableElement().getVersion(caseExecution);
  }

  protected String getDeploymentId(CmmnActivityExecution execution) {
    return getCallableElement().getDeploymentId();
  }

  protected CallableElementBinding getBinding() {
    return getCallableElement().getBinding();
  }

  protected boolean isLatestBinding() {
    return getCallableElement().isLatestBinding();
  }

  protected boolean isDeploymentBinding() {
    return getCallableElement().isDeploymentBinding();
  }

  protected boolean isVersionBinding() {
    return getCallableElement().isVersionBinding();
  }

  protected boolean isVersionTagBinding() {
    return getCallableElement().isVersionTagBinding();
  }

}
