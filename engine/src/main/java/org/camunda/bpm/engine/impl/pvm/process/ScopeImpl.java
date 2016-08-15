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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.core.model.CoreActivity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;


/**
 * A Bpmn scope. The scope has references to two lists of activities:
 * - the flow activities (activities for which the {@link ActivityImpl#getFlowScope() flow scope} is this scope
 * - event listener activities (activities for which the {@link ActivityImpl#getEventScope() event scope} is this scope.
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public abstract class ScopeImpl extends CoreActivity implements PvmScope {

  private static final long serialVersionUID = 1L;

  protected boolean isSubProcessScope = false;

  /** The activities for which the flow scope is this scope  */
  protected List<ActivityImpl> flowActivities = new ArrayList<ActivityImpl>();
  protected Map<String, ActivityImpl> namedFlowActivities = new HashMap<String, ActivityImpl>();

  /** activities for which this is the event scope **/
  protected Set<ActivityImpl> eventActivities = new HashSet<ActivityImpl>();

  protected ProcessDefinitionImpl processDefinition;

  public ScopeImpl(String id, ProcessDefinitionImpl processDefinition) {
    super(id);
    this.processDefinition = processDefinition;
  }

  public ActivityImpl findActivity(String activityId) {
    return (ActivityImpl) super.findActivity(activityId);
  }

  public TransitionImpl findTransition(String transitionId) {
    for (PvmActivity childActivity : flowActivities) {
      for (PvmTransition transition : childActivity.getOutgoingTransitions()) {
        if (transitionId.equals(transition.getId())) {
          return (TransitionImpl) transition;
        }
      }
    }

    for (ActivityImpl childActivity : flowActivities) {
      TransitionImpl nestedTransition = childActivity.findTransition(transitionId);
      if (nestedTransition != null) {
        return nestedTransition;
      }
    }

    return null;
  }

  public ActivityImpl findActivityAtLevelOfSubprocess(String activityId) {
    if(!isSubProcessScope()) {
      throw new ProcessEngineException("This is not a sub process scope.");
    }
    ActivityImpl activity = findActivity(activityId);
    if(activity == null || activity.getLevelOfSubprocessScope() != this) {
      return null;
    }
    else {
      return activity;
    }
  }

  /** searches for the activity locally */
  public ActivityImpl getChildActivity(String activityId) {
    return namedFlowActivities.get(activityId);
  }


  /**
   * Represents the backlog error callback interface.
   * Contains a callback method, which is called if the activity in the backlog
   * is not read till the end of parsing.
   */
  public interface BacklogErrorCallback {
    /**
     * In error case the callback will called.
     */
    public void callback();
  }

  /**
   * The key identifies the activity which is referenced but not read yet.
   * The value is the error callback, which is called if the activity is not
   * read till the end of parsing.
   */
  protected final Map<String, BacklogErrorCallback> BACKLOG = new HashMap<String, BacklogErrorCallback>();

  /**
   * Returns the backlog error callback's.
   *
   * @return the callback's
   */
  public Collection<BacklogErrorCallback> getBacklogErrorCallbacks() {
    return BACKLOG.values();
  }

  /**
   * Returns true if the backlog is empty.
   *
   * @return true if empty, false otherwise
   */
  public boolean isBacklogEmpty() {
    return BACKLOG.isEmpty();
  }

  /**
   * Add's the given activity reference and the error callback to the backlog.
   *
   * @param activityRef the activity reference which is not read until now
   * @param callback the error callback which should called if activity will not be read
   */
  public void addToBacklog(String activityRef, BacklogErrorCallback callback) {
    BACKLOG.put(activityRef, callback);
  }

  public ActivityImpl createActivity(String activityId) {
    ActivityImpl activity = new ActivityImpl(activityId, processDefinition);
    if (activityId!=null) {
      if (processDefinition.findActivity(activityId) != null) {
        throw new PvmException("duplicate activity id '" + activityId + "'");
      }
      if (BACKLOG.containsKey(activityId)) {
        BACKLOG.remove(activityId);
      }
      namedFlowActivities.put(activityId, activity);
    }
    activity.flowScope = this;
    flowActivities.add(activity);

    return  activity;
  }

  public boolean isAncestorFlowScopeOf(ScopeImpl other) {
    ScopeImpl otherAncestor = other.getFlowScope();
    while (otherAncestor != null) {
      if (this == otherAncestor) {
        return true;
      }
      else {
        otherAncestor = otherAncestor.getFlowScope();
      }
    }

    return false;
  }

  public boolean contains(ActivityImpl activity) {
    if (namedFlowActivities.containsKey(activity.getId())) {
      return true;
    }
    for (ActivityImpl nestedActivity : flowActivities) {
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
    return flowActivities;
  }

  public Set<ActivityImpl> getEventActivities() {
    return eventActivities;
  }

  public boolean isSubProcessScope() {
    return isSubProcessScope;
  }

  public void setSubProcessScope(boolean isSubProcessScope) {
    this.isSubProcessScope = isSubProcessScope;
  }

  @Override
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

}
