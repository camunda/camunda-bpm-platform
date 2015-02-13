/* Licensed under the Apache License, ersion 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.pvm.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.BpmnModelExecutionContext;
import org.camunda.bpm.engine.delegate.ProcessEngineServicesAware;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionImpl;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.variable.scope.CoreVariableStore;
import org.camunda.bpm.engine.impl.core.variable.scope.SimpleVariableStore;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class ExecutionImpl extends PvmExecutionImpl implements
        Serializable,
        ActivityExecution,
        PvmProcessInstance {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(ExecutionImpl.class.getName());

  private static AtomicInteger idGenerator = new AtomicInteger();

  // current position /////////////////////////////////////////////////////////

  /** the process instance.  this is the root of the execution tree.
   * the processInstance of a process instance is a self reference. */
  protected ExecutionImpl processInstance;

  /** the parent execution */
  protected ExecutionImpl parent;

  /** nested executions representing scopes or concurrent paths */
  protected List<ExecutionImpl> executions;

  /** super execution, not-null if this execution is part of a subprocess */
  protected ExecutionImpl superExecution;

  /** reference to a subprocessinstance, not-null if currently subprocess is started from this execution */
  protected ExecutionImpl subProcessInstance;

  /** super case execution, not-null if this execution is part of a case execution */
  protected CaseExecutionImpl superCaseExecution;

  /** reference to a subcaseinstance, not-null if currently subcase is started from this execution */
  protected CaseExecutionImpl subCaseInstance;

  protected ExecutionImpl replacedBy;

  // variables/////////////////////////////////////////////////////////////////

  protected SimpleVariableStore variableStore = new SimpleVariableStore();

  // lifecycle methods ////////////////////////////////////////////////////////

  public ExecutionImpl() {
  }

  public ExecutionImpl(ActivityImpl startActivity) {
    super(startActivity);
  }

  /** creates a new execution. properties processDefinition, processInstance and activity will be initialized. */
  public ExecutionImpl createExecution(boolean initializeExecutionStartContext) {
    // create the new child execution
    ExecutionImpl createdExecution = newExecution();

    // manage the bidirectional parent-child relation
    getExecutions().add(createdExecution);
    createdExecution.setParent(this);

    // initialize the new execution
    createdExecution.setProcessDefinition(getProcessDefinition());
    createdExecution.setProcessInstance(getProcessInstance());
    createdExecution.setActivity(getActivity());

    // make created execution start in same activity instance
    createdExecution.activityInstanceId = activityInstanceId;

    if (initializeExecutionStartContext) {
      createdExecution.setStartContext(new ExecutionStartContext());
    } else if (startContext != null) {
      createdExecution.setStartContext(startContext);
    }

    return createdExecution;
  }

  /** instantiates a new execution.  can be overridden by subclasses */
  protected ExecutionImpl newExecution() {
    return new ExecutionImpl();
  }

  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey) {
    ExecutionImpl processInstance = getProcessInstance();
    String caseInstanceId = processInstance.getCaseInstanceId();

    return createSubProcessInstance(processDefinition, businessKey, caseInstanceId);
  }

  public PvmExecutionImpl createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey, String caseInstanceId) {
    ExecutionImpl subProcessInstance = newExecution();

    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperExecution(this);
    this.setSubProcessInstance(subProcessInstance);

    // Initialize the new execution
    subProcessInstance.setProcessDefinition((ProcessDefinitionImpl) processDefinition);
    subProcessInstance.setProcessInstance(subProcessInstance);

    if(businessKey != null) {
      subProcessInstance.setBusinessKey(businessKey);
    }

    if(caseInstanceId != null) {
      subProcessInstance.setCaseInstanceId(caseInstanceId);
    }

    return subProcessInstance;
  }

  public void initialize() {
  }

  public void interruptScope(String reason) {
  }

  // parent ///////////////////////////////////////////////////////////////////

  /** ensures initialization and returns the parent */
  public ExecutionImpl getParent() {
    return parent;
  }

  /** all updates need to go through this setter as subclasses can override this method */
  public void setParent(PvmExecutionImpl parent) {
    this.parent = (ExecutionImpl) parent;
  }

  // executions ///////////////////////////////////////////////////////////////

  /** ensures initialization and returns the non-null executions list */
  public List<ExecutionImpl> getExecutions() {
    if(executions == null) {
      executions = new ArrayList<ExecutionImpl>();
    }
    return executions;
  }

  public ExecutionImpl getSuperExecution() {
    return superExecution;
  }

  public void setSuperExecution(PvmExecutionImpl superExecution) {
    this.superExecution = (ExecutionImpl) superExecution;
    if (superExecution != null) {
      superExecution.setSubProcessInstance(null);
    }
  }

  public ExecutionImpl getSubProcessInstance() {
    return subProcessInstance;
  }

  public void setSubProcessInstance(PvmExecutionImpl subProcessInstance) {
    this.subProcessInstance = (ExecutionImpl) subProcessInstance;
  }

  // super case execution /////////////////////////////////////////////////////

  public CaseExecutionImpl getSuperCaseExecution() {
    return superCaseExecution;
  }

  public void setSuperCaseExecution(CmmnExecution superCaseExecution) {
    this.superCaseExecution = (CaseExecutionImpl) superCaseExecution;
  }

  // sub case execution ////////////////////////////////////////////////////////

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

    // manage bidirectional super-process-sub-case-instances relation
    subCaseInstance.setSuperExecution(this);
    setSubCaseInstance(subCaseInstance);

    return caseInstance;
  }

  // process definition ///////////////////////////////////////////////////////

  public String getProcessDefinitionId() {
    return processDefinition.getId();
  }

  // process instance /////////////////////////////////////////////////////////

  public void start(Map<String, Object> variables) {
    if (isProcessInstanceExecution()) {
      if (processInstanceStartContext == null) {
        processInstanceStartContext = new ProcessInstanceStartContext(processDefinition.getInitial());
      }
    }

    super.start(variables);
  }


  /** ensures initialization and returns the process instance. */
  public ExecutionImpl getProcessInstance() {
    return processInstance;
  }

  public String getProcessInstanceId() {
    return getProcessInstance().getId();
  }

  public String getBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getProcessBusinessKey() {
    return getProcessInstance().getBusinessKey();
  }

  /** for setting the process instance, this setter must be used as subclasses can override */
  public void setProcessInstance(PvmExecutionImpl processInstance) {
    this.processInstance = (ExecutionImpl) processInstance;
  }

  // activity /////////////////////////////////////////////////////////////////

  /**
   * generates an activity instance id
   */
  protected String generateActivityInstanceId(String activityId) {
    int nextId = idGenerator.incrementAndGet();
    String compositeId = activityId+":"+nextId;
    if(compositeId.length()>64) {
      return String.valueOf(nextId);
    } else {
      return compositeId;
    }
  }

  // toString /////////////////////////////////////////////////////////////////

  public String toString() {
    if (isProcessInstanceExecution()) {
      return "ProcessInstance["+getToStringIdentity()+"]";
    } else {
      return (isEventScope? "EventScope":"")+(isConcurrent? "Concurrent" : "")+(isScope() ? "Scope" : "")+"Execution["+getToStringIdentity()+"]";
    }
  }

  protected String getToStringIdentity() {
    return Integer.toString(System.identityHashCode(this));
  }

  // allow for subclasses to expose a real id /////////////////////////////////

  public String getId() {
    return String.valueOf(System.identityHashCode(this));
  }

  // getters and setters //////////////////////////////////////////////////////

  protected CoreVariableStore getVariableStore() {
    return variableStore;
  }

  public PvmExecutionImpl getReplacedBy() {
    return replacedBy;
  }

  public void setReplacedBy(PvmExecutionImpl replacedBy) {
    this.replacedBy = (ExecutionImpl) replacedBy;
    // set execution to this activity instance
    super.setReplacedBy(replacedBy);
  }

  public void setExecutions(List<ExecutionImpl> executions) {
    this.executions = executions;
  }

  public String getCurrentActivityName() {
    String currentActivityName = null;
    if (this.activity != null) {
      currentActivityName = (String) activity.getProperty("name");
    }
    return currentActivityName;
  }

  public FlowElement getBpmnModelElementInstance() {
    throw new UnsupportedOperationException(BpmnModelExecutionContext.class.getName() +" is unsupported in transient ExecutionImpl");
  }

  public BpmnModelInstance getBpmnModelInstance() {
    throw new UnsupportedOperationException(BpmnModelExecutionContext.class.getName() +" is unsupported in transient ExecutionImpl");
  }

  public ProcessEngineServices getProcessEngineServices() {
    throw new UnsupportedOperationException(ProcessEngineServicesAware.class.getName() +" is unsupported in transient ExecutionImpl");
  }

}
