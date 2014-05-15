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
import org.camunda.bpm.engine.delegate.ProcessEngineServicesAware;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableStore;
import org.camunda.bpm.engine.impl.core.variable.SimpleVariableStrore;

/**
 * @author Roman Smirnov
 *
 */
public class PlanItemImpl extends CmmnExecution implements Serializable {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(PlanItemImpl.class.getName());

  private static AtomicInteger idGenerator = new AtomicInteger();

  // current position /////////////////////////////////////////////////////////

  protected List<PlanItemImpl> planItems;

  protected PlanItemImpl caseInstance;

  protected PlanItemImpl parent;

  // variables ////////////////////////////////////////////////////////////////

  protected SimpleVariableStrore variableStrore = new SimpleVariableStrore();

  public PlanItemImpl() {
  }

  // case definition id ///////////////////////////////////////////////////////

  public String getCaseDefinitionId() {
    return getCaseDefinition().getId();
  }

  // parent ////////////////////////////////////////////////////////////////////

  public PlanItemImpl getParent() {
    return parent;
  }

  public void setParent(CmmnExecution parent) {
    this.parent = (PlanItemImpl) parent;
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

  // plan items ////////////////////////////////////////////////////////////////

  public List<PlanItemImpl> getPlanItems() {
    if (planItems == null) {
      planItems = new ArrayList<PlanItemImpl>();
    }
    return planItems;
  }

  // case instance /////////////////////////////////////////////////////////////

  public PlanItemImpl getCaseInstance() {
    return caseInstance;
  }

  public void setCaseInstance(CmmnExecution caseInstance) {
    this.caseInstance = (PlanItemImpl) caseInstance;
  }

  // activity instance id //////////////////////////////////////////////////////

  protected String generateActivityInstanceId(String activityId) {
    int nextId = idGenerator.incrementAndGet();
    String compositeId = activityId+":"+nextId;
    if(compositeId.length()>64) {
      return String.valueOf(nextId);
    } else {
      return compositeId;
    }
  }

  public ProcessEngineServices getProcessEngineServices() {
    throw new UnsupportedOperationException(ProcessEngineServicesAware.class.getName() +" is unsupported in transient PlanItemImpl");
  }

  // new plan items /////////////////////////////////////////////////////////

  protected PlanItemImpl createPlanItem(CmmnActivity activity) {
    PlanItemImpl child = newPlanItem();

    // TODO: evaluate "RepetitionRule" and "RequiredRule"

    // set activity to execute
    child.setActivity(activity);

    // handle child/parent-relation
    child.setParent(this);
    getPlanItems().add(child);

    // set case instance
    child.setCaseInstance(getCaseInstance());

    // set case definition
    child.setCaseDefinition(getCaseDefinition());

    // set state to available
    child.setState(PlanItemState.AVAILABLE);

    // set activity instance id
    activityInstanceId = child.generateActivityInstanceId(activity.getId());

    if (log.isLoggable(Level.FINE)) {
      log.fine("Child planItem " + child + " created with parent " + this);
    }

    return child;
  };

  protected PlanItemImpl newPlanItem() {
    return new PlanItemImpl();
  }

  // variables //////////////////////////////////////////////////////////////

  protected CoreVariableStore getVariableStore() {
    return variableStrore;
  }

  // toString /////////////////////////////////////////////////////////////////

  public String toString() {
    if (isCaseInstance()) {
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

}
