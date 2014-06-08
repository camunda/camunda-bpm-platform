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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.CmmnModelExecutionContext;
import org.camunda.bpm.engine.delegate.ProcessEngineServicesAware;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableStore;
import org.camunda.bpm.engine.impl.core.variable.SimpleVariableStore;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionImpl extends CmmnExecution implements Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(CaseExecutionImpl.class.getName());

  private static AtomicInteger idGenerator = new AtomicInteger();

  // current position /////////////////////////////////////////////////////////

  protected List<CaseExecutionImpl> caseExecutions;

  protected CaseExecutionImpl caseInstance;

  protected CaseExecutionImpl parent;

  // variables ////////////////////////////////////////////////////////////////

  protected SimpleVariableStore variableStrore = new SimpleVariableStore();

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


  // new case executions /////////////////////////////////////////////////////////

  protected CaseExecutionImpl createCaseExecution(CmmnActivity activity) {
    CaseExecutionImpl child = newCaseExecution();

    // TODO: evaluate "RepetitionRule" and "RequiredRule"

    // set activity to execute
    child.setActivity(activity);

    // handle child/parent-relation
    child.setParent(this);
    getCaseExecutions().add(child);

    // set case instance
    child.setCaseInstance(getCaseInstance());

    // set case definition
    child.setCaseDefinition(getCaseDefinition());

    // set state to available
    child.setCurrentState(CaseExecutionState.AVAILABLE);

    if (log.isLoggable(Level.FINE)) {
      log.fine("Child caseExecution " + child + " created with parent " + this);
    }

    return child;
  };

  protected CaseExecutionImpl newCaseExecution() {
    return new CaseExecutionImpl();
  }

  // variables //////////////////////////////////////////////////////////////

  protected CoreVariableStore getVariableStore() {
    return variableStrore;
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
    throw new UnsupportedOperationException(ProcessEngineServicesAware.class.getName() +" is unsupported in transient CaseExecutionImpl");
  }

  public CmmnElement getCmmnModelElementInstance() {
    throw new UnsupportedOperationException(CmmnModelExecutionContext.class.getName() +" is unsupported in transient CaseExecutionImpl");
  }

  public CmmnModelInstance getCmmnModelInstance() {
    throw new UnsupportedOperationException(CmmnModelExecutionContext.class.getName() +" is unsupported in transient CaseExecutionImpl");
  }

}
