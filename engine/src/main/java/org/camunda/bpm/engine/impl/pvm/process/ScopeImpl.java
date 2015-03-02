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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.core.model.CoreActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public abstract class ScopeImpl extends CoreActivity implements PvmScope {

  private static final long serialVersionUID = 1L;

  protected List<ActivityImpl> activities = new ArrayList<ActivityImpl>();
  protected Map<String, ActivityImpl> namedActivities = new HashMap<String, ActivityImpl>();

  protected ProcessDefinitionImpl processDefinition;

  public ScopeImpl(String id, ProcessDefinitionImpl processDefinition) {
    super(id);
    this.processDefinition = processDefinition;
  }

  public ActivityImpl findActivity(String activityId) {
    return (ActivityImpl) super.findActivity(activityId);
  }

  public TransitionImpl findTransition(String transitionId) {
    for (ActivityImpl childActivity : activities) {
      for (PvmTransition transition : childActivity.getOutgoingTransitions()) {
        if (transitionId.equals(transition.getId())) {
          return (TransitionImpl) transition;
        }
      }
    }

    for (ActivityImpl childActivity : activities) {
      TransitionImpl nestedTransition = childActivity.findTransition(transitionId);
      if (nestedTransition != null) {
        return nestedTransition;
      }
    }

    return null;
  }

  /** searches for the activity locally */
  public ActivityImpl getChildActivity(String activityId) {
    return namedActivities.get(activityId);
  }

  public ActivityImpl createActivity(String activityId) {
    ActivityImpl activity = new ActivityImpl(activityId, processDefinition);
    if (activityId!=null) {
      if (processDefinition.findActivity(activityId) != null) {
        throw new PvmException("duplicate activity id '" + activityId + "'");
      }
      namedActivities.put(activityId, activity);
    }
    activity.setParent(this);
    activities.add(activity);
    return  activity;
  }

  public boolean contains(ActivityImpl activity) {
    if (namedActivities.containsKey(activity.getId())) {
      return true;
    }
    for (ActivityImpl nestedActivity : activities) {
      if (nestedActivity.contains(activity)) {
        return true;
      }
    }
    return false;
  }

  // event listeners //////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  @Deprecated
  public List<ExecutionListener> getExecutionListeners(String eventName) {
    return (List) super.getListeners(eventName);
  }

  @Deprecated
  public void addExecutionListener(String eventName, ExecutionListener executionListener) {
    super.addListener(eventName, executionListener);
  }

  @Deprecated
  public void addExecutionListener(String eventName, ExecutionListener executionListener, int index) {
    super.addListener(eventName, executionListener, index);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Deprecated
  public Map<String, List<ExecutionListener>> getExecutionListeners() {
    return (Map) super.getListeners();
  }

  // getters and setters //////////////////////////////////////////////////////

  public List<ActivityImpl> getActivities() {
    return activities;
  }

  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  public abstract ScopeImpl getParent();

  public abstract ScopeImpl getParentScope();

  public abstract boolean isScope();

}
