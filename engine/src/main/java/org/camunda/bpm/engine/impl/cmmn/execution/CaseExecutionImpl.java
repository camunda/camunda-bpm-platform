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
package org.camunda.bpm.engine.impl.cmmn.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.CmmnModelExecutionContext;
import org.camunda.bpm.engine.delegate.ProcessEngineServicesAware;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnBehaviorLogger;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.SimpleVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.SimpleVariableInstance.SimpleVariableInstanceFactory;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceFactory;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceLifecycleListener;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableStore;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionImpl extends CmmnExecution implements Serializable {

  protected static final CmmnBehaviorLogger LOG = ProcessEngineLogger.CMNN_BEHAVIOR_LOGGER;

  private static final long serialVersionUID = 1L;

  // current position /////////////////////////////////////////////////////////

  protected List<CaseExecutionImpl> caseExecutions;

  protected List<CaseSentryPartImpl> caseSentryParts;

  protected CaseExecutionImpl caseInstance;

  protected CaseExecutionImpl parent;

  protected ExecutionImpl subProcessInstance;

  protected ExecutionImpl superExecution;

  protected CaseExecutionImpl subCaseInstance;

  protected CaseExecutionImpl superCaseExecution;

  // variables ////////////////////////////////////////////////////////////////

  protected VariableStore<SimpleVariableInstance> variableStore = new VariableStore<SimpleVariableInstance>();

  public CaseExecutionImpl() {
  }

  // case definition id ///////////////////////////////////////////////////////

  public String getCaseDefinitionId() {
    return getCaseDefinition().getId();
  }

  // parent ////////////////////////////////////////////////////////////////////

  public CaseExecutionImpl getParent() {
    return parent;
  }

  public void setParent(CmmnExecution parent) {
    this.parent = (CaseExecutionImpl) parent;
  }

  public String getParentId() {
    return getParent().getId();
  }

  // activity //////////////////////////////////////////////////////////////////

  public String getActivityId() {
    return getActivity().getId();
  }

  public String getActivityName() {
    return getActivity().getName();
  }

  // case executions ////////////////////////////////////////////////////////////////

  public List<CaseExecutionImpl> getCaseExecutions() {
    return new ArrayList<CaseExecutionImpl>(getCaseExecutionsInternal());
  }

  protected List<CaseExecutionImpl> getCaseExecutionsInternal() {
    if (caseExecutions == null) {
      caseExecutions = new ArrayList<CaseExecutionImpl>();
    }
    return caseExecutions;
  }

  // case instance /////////////////////////////////////////////////////////////

  public CaseExecutionImpl getCaseInstance() {
    return caseInstance;
  }

  public void setCaseInstance(CmmnExecution caseInstance) {
    this.caseInstance = (CaseExecutionImpl) caseInstance;
  }

  // super execution /////////////////////////////////////////////////////////////

  public ExecutionImpl getSuperExecution() {
    return superExecution;
  }

  public void setSuperExecution(PvmExecutionImpl superExecution) {
    this.superExecution = (ExecutionImpl) superExecution;
  }

  // sub process instance ////////////////////////////////////////////////////////

  public ExecutionImpl getSubProcessInstance() {
    return subProcessInstance;
  }

  public void setSubProcessInstance(PvmExecutionImpl subProcessInstance) {
    this.subProcessInstance = (ExecutionImpl) subProcessInstance;
  }

  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition) {
    return createSubProcessInstance(processDefinition, null);
  }

  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey) {
    return createSubProcessInstance(processDefinition, businessKey, getCaseInstanceId());
  }

  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey, String caseInstanceId) {
    ExecutionImpl subProcessInstance = (ExecutionImpl) processDefinition.createProcessInstance(businessKey, caseInstanceId);

    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperCaseExecution(this);
    setSubProcessInstance(subProcessInstance);

    return subProcessInstance;
  }

  // sub-/super- case instance ////////////////////////////////////////////////////

  public CaseExecutionImpl getSubCaseInstance() {
    return subCaseInstance;
  }

  public void setSubCaseInstance(CmmnExecution subCaseInstance) {
    this.subCaseInstance = (CaseExecutionImpl) subCaseInstance;
  }

  public CaseExecutionImpl createSubCaseInstance(CmmnCaseDefinition caseDefinition) {
    return createSubCaseInstance(caseDefinition, null);
  }

  public CaseExecutionImpl createSubCaseInstance(CmmnCaseDefinition caseDefinition, String businessKey) {
    CaseExecutionImpl caseInstance = (CaseExecutionImpl) caseDefinition.createCaseInstance(businessKey);

    // manage bidirectional super-sub-case-instances relation
    subCaseInstance.setSuperCaseExecution(this);
    setSubCaseInstance(subCaseInstance);

    return caseInstance;
  }

  public CaseExecutionImpl getSuperCaseExecution() {
    return superCaseExecution;
  }

  public void setSuperCaseExecution(CmmnExecution superCaseExecution) {
    this.superCaseExecution = (CaseExecutionImpl) superCaseExecution;
  }

  // sentry /////////////////////////////////////////////////////////////////////////

  public List<CaseSentryPartImpl> getCaseSentryParts() {
    if (caseSentryParts == null) {
      caseSentryParts = new ArrayList<CaseSentryPartImpl>();
    }
    return caseSentryParts;
  }

  protected Map<String, List<CmmnSentryPart>> getSentries() {
    Map<String, List<CmmnSentryPart>> sentries = new HashMap<String, List<CmmnSentryPart>>();

    for (CaseSentryPartImpl sentryPart : getCaseSentryParts()) {

      String sentryId = sentryPart.getSentryId();
      List<CmmnSentryPart> parts = sentries.get(sentryId);

      if (parts == null) {
        parts = new ArrayList<CmmnSentryPart>();
        sentries.put(sentryId, parts);
      }

      parts.add(sentryPart);
    }

    return sentries;
  }

  protected List<CaseSentryPartImpl> findSentry(String sentryId) {
    List<CaseSentryPartImpl> result = new ArrayList<CaseSentryPartImpl>();

    for (CaseSentryPartImpl sentryPart : getCaseSentryParts()) {
      if (sentryPart.getSentryId().equals(sentryId)) {
        result.add(sentryPart);
      }
    }

    return result;
  }

  protected void addSentryPart(CmmnSentryPart sentryPart) {
    getCaseSentryParts().add((CaseSentryPartImpl) sentryPart);
  }

  protected CmmnSentryPart newSentryPart() {
    return new CaseSentryPartImpl();
  }

  // new case executions ////////////////////////////////////////////////////////////

  protected CaseExecutionImpl createCaseExecution(CmmnActivity activity) {
    CaseExecutionImpl child = newCaseExecution();

    // set activity to execute
    child.setActivity(activity);

    // handle child/parent-relation
    child.setParent(this);
    getCaseExecutionsInternal().add(child);

    // set case instance
    child.setCaseInstance(getCaseInstance());

    // set case definition
    child.setCaseDefinition(getCaseDefinition());

    return child;
  }

  protected CaseExecutionImpl newCaseExecution() {
    return new CaseExecutionImpl();
  }

  // variables //////////////////////////////////////////////////////////////

  protected VariableStore<CoreVariableInstance> getVariableStore() {
    return (VariableStore) variableStore;
  }

  @Override
  protected VariableInstanceFactory<CoreVariableInstance> getVariableInstanceFactory() {
    return (VariableInstanceFactory) SimpleVariableInstanceFactory.INSTANCE;
  }

  @Override
  protected List<VariableInstanceLifecycleListener<CoreVariableInstance>> getVariableInstanceLifecycleListeners() {
    return Collections.emptyList();
  }

  // toString /////////////////////////////////////////////////////////////////

  public String toString() {
    if (isCaseInstanceExecution()) {
      return "CaseInstance[" + getToStringIdentity() + "]";
    } else {
      return "CmmnExecution["+getToStringIdentity() + "]";
    }
  }

  protected String getToStringIdentity() {
    return Integer.toString(System.identityHashCode(this));
  }

  public String getId() {
    return String.valueOf(System.identityHashCode(this));
  }

  public ProcessEngineServices getProcessEngineServices() {
    throw LOG.unsupportedTransientOperationException(ProcessEngineServicesAware.class.getName());
  }

  public CmmnElement getCmmnModelElementInstance() {
    throw LOG.unsupportedTransientOperationException(CmmnModelExecutionContext.class.getName());
  }

  public CmmnModelInstance getCmmnModelInstance() {
    throw LOG.unsupportedTransientOperationException(CmmnModelExecutionContext.class.getName());
  }
}
