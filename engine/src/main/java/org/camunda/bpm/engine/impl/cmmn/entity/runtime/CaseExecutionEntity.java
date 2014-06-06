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
package org.camunda.bpm.engine.impl.cmmn.entity.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionState;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableStore;
import org.camunda.bpm.engine.impl.db.HasRevision;
import org.camunda.bpm.engine.impl.db.PersistentObject;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionEntity extends CmmnExecution implements CaseExecution, CaseInstance, PersistentObject, HasRevision {

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(CmmnExecution.class.getName());


  // current position /////////////////////////////////////////////////////////

  /** the case instance.  this is the root of the execution tree.
   * the caseInstance of a case instance is a self reference. */
  protected transient CaseExecutionEntity caseInstance;

  /** the parent execution */
  protected transient CaseExecutionEntity parent;

  /** nested executions */
  protected List<CaseExecutionEntity> caseExecutions;

  // associated entities /////////////////////////////////////////////////////

  protected CaseExecutionEntityVariableStore variableStore = new CaseExecutionEntityVariableStore(this);

  // Persistence //////////////////////////////////////////////////////////////

  protected int revision = 1;
  protected String caseDefinitionId;
  protected String activityId;
  protected String activityName;
  protected String caseInstanceId;
  protected String parentId;

  // case definition ///////////////////////////////////////////////////////////

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  /** ensures initialization and returns the case definition. */
  public CmmnCaseDefinition getCaseDefinition() {
    ensureCaseDefinitionInitialized();
    return caseDefinition;
  }

  public void setCaseDefinition(CmmnCaseDefinition caseDefinition) {
    super.setCaseDefinition(caseDefinition);

    caseDefinitionId = null;
    if (caseDefinition != null) {
      caseDefinitionId = caseDefinition.getId();
    }

  }

  protected void ensureCaseDefinitionInitialized() {
    if ((caseDefinition == null) && (caseDefinitionId != null)) {

      CaseDefinitionEntity deployedCaseDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .findDeployedCaseDefinitionById(caseDefinitionId);

      setCaseDefinition(deployedCaseDefinition);
    }
  }

  // parent ////////////////////////////////////////////////////////////////////

  public CaseExecutionEntity getParent() {
    ensureParentInitialized();
    return parent;
  }

  public void setParent(CmmnExecution parent) {
    this.parent = (CaseExecutionEntity) parent;

    if (parent != null) {
      this.parentId = parent.getId();
    } else {
      this.parentId = null;
    }
  }

  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {

      parent = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findCaseExecutionById(parentId);
    }
  }

  public String getParentId() {
    return parentId;
  }

  // activity //////////////////////////////////////////////////////////////////

  public String getActivityId() {
    return activityId;
  }

  public String getActivityName() {
    return activityName;
  }

  public CmmnActivity getActivity() {
    ensureActivityInitialized();
    return super.getActivity();
  }

  public void setActivity(CmmnActivity activity) {
    super.setActivity(activity);
    if (activity != null) {
      this.activityId = activity.getId();
      this.activityName = (String) activity.getProperty("name");
    } else {
      this.activityId = null;
      this.activityName = null;
    }
  }

  protected void ensureActivityInitialized() {
    if ((activity == null) && (activityId != null)) {
      setActivity(getCaseDefinition().findActivity(activityId));
    }
  }

  // case executions ////////////////////////////////////////////////////////////////

  public List<CaseExecutionEntity> getCaseExecutions() {
    ensureCaseExecutionsInitialized();
    return caseExecutions;
  }

  protected void ensureCaseExecutionsInitialized() {
    if (caseExecutions == null) {
      this.caseExecutions = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findChildCaseExecutionsByParentCaseExecutionId(id);
    }
  }

  // case instance /////////////////////////////////////////////////////////

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public CaseExecutionEntity getCaseInstance() {
    ensureCaseInstanceInitialized();
    return caseInstance;
  }

  public void setCaseInstance(CmmnExecution caseInstance) {
    this.caseInstance = (CaseExecutionEntity) caseInstance;

    if (caseInstance != null) {
      this.caseInstanceId = this.caseInstance.getId();
    }
  }

  protected void ensureCaseInstanceInitialized() {
    if ((caseInstance == null) && (caseInstanceId != null)) {

      caseInstance =  Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findCaseExecutionById(caseInstanceId);

    }
  }

  @Override
  public boolean isCaseInstanceExecution() {
    return parentId == null;
  }

  protected CaseExecutionEntity createCaseExecution(CmmnActivity activity) {
    CaseExecutionEntity child = newCaseExecution();

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

  protected CaseExecutionEntity newCaseExecution() {
    CaseExecutionEntity newCaseExecution = new CaseExecutionEntity();

    Context
      .getCommandContext()
      .getCaseExecutionManager()
      .insertCaseExecution(newCaseExecution);

    return newCaseExecution;
  }

  // variables //////////////////////////////////////////////////////////////

  protected CoreVariableStore getVariableStore() {
    return variableStore;
  }

  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setCaseInstanceId(caseInstanceId);
    variableInstance.setCaseExecutionId(id);
  }

  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context
        .getCommandContext()
        .getVariableInstanceManager()
        .findVariableInstancesByCaseExecutionId(id);
  }

  // toString /////////////////////////////////////////////////////////////

  public String toString() {
    if (isCaseInstanceExecution()) {
      return "CaseInstance["+getToStringIdentity()+"]";
    } else {
      return "CaseExecution["+getToStringIdentity()+"]";
    }
  }

  protected String getToStringIdentity() {
    return id;
  }

  // delete/remove ///////////////////////////////////////////////////////

  public void remove() {
    super.remove();

    variableStore.removeVariablesWithoutFiringEvents();

    // finally delete this execution
    Context.getCommandContext()
      .getCaseExecutionManager()
      .deleteCaseExecution(this);
  }

  // persistence /////////////////////////////////////////////////////////

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("caseDefinitionId", caseDefinitionId);
    persistentState.put("businessKey", businessKey);
    persistentState.put("activityId", activityId);
    persistentState.put("parentId", parentId);
    persistentState.put("currentState", currentState);
    persistentState.put("previousState", previousState);
    return persistentState;
  }

  public CmmnModelInstance getCmmnModelInstance() {
    if(caseDefinitionId != null) {

      return Context.getProcessEngineConfiguration()
        .getDeploymentCache()
        .findCmmnModelInstanceForCaseDefinition(caseDefinitionId);

    } else {
      return null;

    }
  }

  public CmmnElement getCmmnModelElementInstance() {
    CmmnModelInstance cmmnModelInstance = getCmmnModelInstance();
    if(cmmnModelInstance != null) {

      ModelElementInstance modelElementInstance = cmmnModelInstance.getModelElementById(activityId);

      return (CmmnElement) modelElementInstance;

    } else {
      return null;
    }
  }

  public ProcessEngineServices getProcessEngineServices() {
    return Context
        .getProcessEngineConfiguration()
        .getProcessEngine();
  }

}
