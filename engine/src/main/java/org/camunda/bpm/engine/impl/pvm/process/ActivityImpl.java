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

  protected ActivityBehavior activityBehavior;

  protected ScopeImpl parent;

  protected boolean isScope;

  protected boolean isAsyncBefore;

  protected boolean isAsyncAfter;

  protected boolean isCancelScope = false;

  protected boolean isConcurrent = false;

  protected ScopeImpl scope;

  protected ScopeImpl flowScope;

  // Graphical information
  protected int x = -1;
  protected int y = -1;
  protected int width = -1;
  protected int height = -1;

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

  public ActivityImpl getParentActivity() {
    if (parent instanceof ActivityImpl) {
      return (ActivityImpl) parent;
    }
    return null;
  }


  // restricted setters ///////////////////////////////////////////////////////

  protected void setOutgoingTransitions(List<TransitionImpl> outgoingTransitions) {
    this.outgoingTransitions = outgoingTransitions;
  }

  protected void setParent(ScopeImpl parent) {
    this.parent = parent;
  }

  public void setScope(ScopeImpl scope) {
    this.scope = scope;
  }

  protected void setIncomingTransitions(List<TransitionImpl> incomingTransitions) {
    this.incomingTransitions = incomingTransitions;
  }

  // getters and setters //////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public List<PvmTransition> getOutgoingTransitions() {
    return (List) outgoingTransitions;
  }

  public ActivityBehavior getActivityBehavior() {
    return activityBehavior;
  }

  public void setActivityBehavior(ActivityBehavior activityBehavior) {
    this.activityBehavior = activityBehavior;
  }

  public ScopeImpl getParent() {
    return parent;
  }

  public ScopeImpl getScope() {
    if(scope == null) {
      return parent;
    } else {
      return scope;
    }
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

  /**
   * Deprecated since 7.2, use {@link #isAsyncBefore()}
   */
  @Deprecated
  public boolean isAsync() {
    return isAsyncBefore;
  }

  /**
   * Deprecated since 7.2, use {@link #setAsyncBefore(boolean)}
   */
  @Deprecated
  public void setAsync(boolean isAsync) {
    this.isAsyncBefore = isAsync;
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

  /**
   * @return Event scope of this activity;
   * An execution of the event scope is responsible for
   * handling events defined by this activity. This is often
   * the same as the flow scope (@see {@link #getFlowScope()}
   * but not necessarily (cf boundary events)
   */
  public ScopeImpl getParentScope() {
    ScopeImpl parentScope = parent;
    while (parentScope != null && !parentScope.isScope()) {
      parentScope = parentScope.getParent();
    }
    return parentScope;
  }

  public ActivityImpl getParentScopeActivity() {
    ScopeImpl parentScope = getParentScope();
    if (parentScope instanceof ActivityImpl) {
      return (ActivityImpl) parentScope;
    }
    return null;
  }

  public boolean isCancelScope() {
    return isCancelScope;
  }

  public void setCancelScope(boolean isInterrupting) {
    this.isCancelScope = isInterrupting;
  }

  public boolean isConcurrent() {
    return isConcurrent;
  }

  public void setConcurrent(boolean isConcurrent) {
    this.isConcurrent = isConcurrent;
  }

  /**
   * @return the scope which should be used for traversing
   * transitions which originate in this activity. This means, the
   * an execution of the flow scope is used to traverse outgoing transitions (without concurrency,
   * this would be the scope execution of the flow scope).
   */
  public ScopeImpl getFlowScope() {
    if(flowScope == null) {
      return getScope();

    } else {

      return flowScope;
    }
  }

  public void setFlowScope(ScopeImpl flowScope) {
    this.flowScope = flowScope;
  }

}
