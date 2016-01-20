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

package org.camunda.bpm.engine.impl.pvm.process;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.impl.core.delegate.CoreActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ProcessDefinitionImpl extends ScopeImpl implements PvmProcessDefinition {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected String description;
  protected ActivityImpl initial;
  protected Map<ActivityImpl, List<ActivityImpl>> initialActivityStacks = new HashMap<ActivityImpl, List<ActivityImpl>>();
  protected List<LaneSet> laneSets;
  protected ParticipantProcess participantProcess;

  public ProcessDefinitionImpl(String id) {
    super(id, null);
    processDefinition = this;
    // the process definition is always "a sub process scope"
    isSubProcessScope = true;
  }

  protected void ensureDefaultInitialExists() {
    ensureNotNull("Process '" + name + "' has no default start activity (e.g. none start event), hence you cannot use 'startProcessInstanceBy...' but have to start it using one of the modeled start events (e.g. message start events)", "initial", initial);
  }

  public PvmProcessInstance createProcessInstance() {
    ensureDefaultInitialExists();
    return createProcessInstance(null, null, initial);
  }

  public PvmProcessInstance createProcessInstance(String businessKey) {
    ensureDefaultInitialExists();
    return createProcessInstance(businessKey, null, this.initial);
  }

  public PvmProcessInstance createProcessInstance(String businessKey, String caseInstanceId) {
    ensureDefaultInitialExists();
    return createProcessInstance(businessKey, caseInstanceId, this.initial);
  }

  public PvmProcessInstance createProcessInstance(String businessKey, ActivityImpl initial) {
    return createProcessInstance(businessKey, null, initial);
  }

  public PvmProcessInstance createProcessInstance(String businessKey, String caseInstanceId, ActivityImpl initial) {
    PvmExecutionImpl processInstance = (PvmExecutionImpl) createProcessInstanceForInitial(initial);

    processInstance.setBusinessKey(businessKey);
    processInstance.setCaseInstanceId(caseInstanceId);

    return processInstance;
  }

  /** creates a process instance using the provided activity as initial */
  public PvmProcessInstance createProcessInstanceForInitial(ActivityImpl initial) {
    ensureNotNull("Cannot start process instance, initial activity where the process instance should start is null", "initial", initial);

    PvmExecutionImpl processInstance = newProcessInstance();

    processInstance.setProcessDefinition(this);

    processInstance.setProcessInstance(processInstance);

    // always set the process instance to the initial activity, no matter how deeply it is nested;
    // this is required for firing history events (cf start activity) and persisting the initial activity
    // on async start
    processInstance.setActivity(initial);

    return processInstance;
  }

  protected PvmExecutionImpl newProcessInstance() {
    return new ExecutionImpl();
  }

  public List<ActivityImpl> getInitialActivityStack() {
    return getInitialActivityStack(initial);
  }

  public synchronized List<ActivityImpl> getInitialActivityStack(ActivityImpl startActivity) {
    List<ActivityImpl> initialActivityStack = initialActivityStacks.get(startActivity);
    if(initialActivityStack == null) {
      initialActivityStack = new ArrayList<ActivityImpl>();
      ActivityImpl activity = startActivity;
      while (activity!=null) {
        initialActivityStack.add(0, activity);
        activity = activity.getParentFlowScopeActivity();
      }
      initialActivityStacks.put(startActivity, initialActivityStack);
    }
    return initialActivityStack;
  }

  public String getDiagramResourceName() {
    return null;
  }

  public String getDeploymentId() {
    return null;
  }

  public void addLaneSet(LaneSet newLaneSet) {
    getLaneSets().add(newLaneSet);
  }

  public Lane getLaneForId(String id) {
    if(laneSets != null && laneSets.size() > 0) {
      Lane lane;
      for(LaneSet set : laneSets) {
        lane = set.getLaneForId(id);
        if(lane != null) {
          return lane;
        }
      }
    }
    return null;
  }

  @Override
  public CoreActivityBehavior<? extends BaseDelegateExecution> getActivityBehavior() {
    // unsupported in PVM
    return null;
  }

  // getters and setters //////////////////////////////////////////////////////

  public ActivityImpl getInitial() {
    return initial;
  }

  public void setInitial(ActivityImpl initial) {
    this.initial = initial;
  }

  @Override
  public String toString() {
    return "ProcessDefinition("+id+")";
  }

   public String getDescription() {
    return (String) getProperty("documentation");
  }

  /**
   * @return all lane-sets defined on this process-instance. Returns an empty list if none are defined.
   */
  public List<LaneSet> getLaneSets() {
    if(laneSets == null) {
      laneSets = new ArrayList<LaneSet>();
    }
    return laneSets;
  }


  public void setParticipantProcess(ParticipantProcess participantProcess) {
    this.participantProcess = participantProcess;
  }

  public ParticipantProcess getParticipantProcess() {
    return participantProcess;
  }

  public boolean isScope() {
    return true;
  }

  public PvmScope getEventScope() {
    return null;
  }

  public ScopeImpl getFlowScope() {
    return null;
  }

  public PvmScope getLevelOfSubprocessScope() {
    return null;
  }

  @Override
  public boolean isSubProcessScope() {
    return true;
  }

}
