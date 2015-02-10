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

import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.impl.pvm.PvmScope;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ActivityImpl extends ScopeImpl implements PvmActivity, HasDIBounds {

  private static final long serialVersionUID = 1L;

  protected List<TransitionImpl> outgoingTransitions = new ArrayList<TransitionImpl>();
  protected Map<String, TransitionImpl> namedOutgoingTransitions = new HashMap<String, TransitionImpl>();
  protected List<TransitionImpl> incomingTransitions = new ArrayList<TransitionImpl>();

  /** the inner behavior of an activity. For activities which are flow scopes,
   * this must be a CompositeActivityBehavior. */
  protected ActivityBehavior activityBehavior;

  /** The start behavior for this activity. */
  protected ActivityStartBehavior activityStartBehavior = ActivityStartBehavior.DEFAULT;

  protected ScopeImpl eventScope;
  protected ScopeImpl flowScope;

  protected boolean isScope = false;

  protected boolean isAsyncBefore;
  protected boolean isAsyncAfter;

  public ActivityImpl(String id, ProcessDefinitionImpl processDefinition) {
    super(id, processDefinition);
  }

  public TransitionImpl createOutgoingTransition() {
    return createOutgoingTransition(null);
  }

  public TransitionImpl createOutgoingTransition(String transitionId) {
    TransitionImpl transition = new TransitionImpl(transitionId, processDefinition);
    transition.setSource(this);
    outgoingTransitions.add(transition);

    if (transitionId!=null) {
      if (namedOutgoingTransitions.containsKey(transitionId)) {
        throw new PvmException("activity '"+id+" has duplicate transition '"+transitionId+"'");
      }
      namedOutgoingTransitions.put(transitionId, transition);
    }

    return transition;
  }

  public TransitionImpl findOutgoingTransition(String transitionId) {
    return namedOutgoingTransitions.get(transitionId);
  }

  public String toString() {
    return "Activity("+id+")";
  }

  // restricted setters ///////////////////////////////////////////////////////

  protected void setOutgoingTransitions(List<TransitionImpl> outgoingTransitions) {
    this.outgoingTransitions = outgoingTransitions;
  }

  protected void setIncomingTransitions(List<TransitionImpl> incomingTransitions) {
    this.incomingTransitions = incomingTransitions;
  }

  // getters and setters //////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public List<PvmTransition> getOutgoingTransitions() {
    return (List) outgoingTransitions;
  }

  @Override
  public ActivityBehavior getActivityBehavior() {
    return activityBehavior;
  }

  public void setActivityBehavior(ActivityBehavior activityBehavior) {
    this.activityBehavior = activityBehavior;
  }

  public ActivityStartBehavior getActivityStartBehavior() {
    return activityStartBehavior;
  }

  public void setActivityStartBehavior(ActivityStartBehavior activityStartBehavior) {
    this.activityStartBehavior = activityStartBehavior;
  }

  @SuppressWarnings("unchecked")
  public List<PvmTransition> getIncomingTransitions() {
    return (List) incomingTransitions;
  }

  public boolean isScope() {
    return isScope;
  }

  public void setScope(boolean isScope) {
    this.isScope = isScope;
  }

  public boolean isAsyncBefore() {
    return isAsyncBefore;
  }

  public void setAsyncBefore(boolean isAsyncBefore) {
    this.isAsyncBefore = isAsyncBefore;
  }

  public boolean isAsyncAfter() {
    return isAsyncAfter;
  }

  public void setAsyncAfter(boolean isAsyncAfter) {
    this.isAsyncAfter = isAsyncAfter;
  }

  public String getActivityId() {
    return super.getId();
  }

  public ScopeImpl getFlowScope() {
    return flowScope;
  }

  public ScopeImpl getEventScope() {
    return eventScope;
  }

  public void setEventScope(ScopeImpl eventScope) {
    this.eventScope = eventScope;
  }

  public PvmScope getLevelOfSubprocessScope() {
    ScopeImpl levelOfSubprocessScope = getFlowScope();
    while(!levelOfSubprocessScope.isSubProcessScope) {
      // cast always possible since process definition is always a sub process scope
      levelOfSubprocessScope = ((PvmActivity)levelOfSubprocessScope).getFlowScope();
    }
    return levelOfSubprocessScope;
  }

  // Graphical information ///////////////////////////////////////////

  protected int x = -1;
  protected int y = -1;
  protected int width = -1;
  protected int height = -1;

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public ActivityImpl getParentFlowScopeActivity() {
    ScopeImpl flowScope = getFlowScope();
    if(flowScope != getProcessDefinition()) {
      return (ActivityImpl) flowScope;
    }
    else {
      return null;
    }
  }

}
