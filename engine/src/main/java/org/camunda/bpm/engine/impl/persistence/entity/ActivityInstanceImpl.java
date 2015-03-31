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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;

/**
 *
 * @author Daniel Meyer
 *
 */
public class ActivityInstanceImpl extends ProcessElementInstanceImpl implements ActivityInstance {

  protected String businessKey;
  protected String activityId;
  protected String activityName;
  protected String activityType;

  protected ActivityInstance[] childActivityInstances = new ActivityInstance[0];
  protected TransitionInstance[] childTransitionInstances = new TransitionInstance[0];

  protected String[] executionIds = new String[0];

  public ActivityInstance[] getChildActivityInstances() {
    return childActivityInstances;
  }

  public void setChildActivityInstances(ActivityInstance[] childInstances) {
    this.childActivityInstances = childInstances;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String[] getExecutionIds() {
    return executionIds;
  }

  public void setExecutionIds(String[] executionIds) {
    this.executionIds = executionIds;
  }

  public TransitionInstance[] getChildTransitionInstances() {
    return childTransitionInstances;
  }

  public void setChildTransitionInstances(TransitionInstance[] childTransitionInstances) {
    this.childTransitionInstances = childTransitionInstances;
  }

  public String getActivityType() {
    return activityType;
  }

  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  protected void writeTree(StringWriter writer, String prefix, boolean isTail) {
    writer.append(prefix);
    if(isTail) {
      writer.append("└── ");
    } else {
      writer.append("├── ");
    }

    writer.append(getActivityId()+"=>"+getId() +"\n");

    for (int i = 0; i < childTransitionInstances.length; i++) {
      TransitionInstance transitionInstance = childTransitionInstances[i];
      boolean transitionIsTail = (i==(childTransitionInstances.length-1))
          && (childActivityInstances.length == 0);
      writeTransition(transitionInstance, writer, prefix +  (isTail ? "    " : "│   "), transitionIsTail);
    }

    for (int i = 0; i < childActivityInstances.length; i++) {
      ActivityInstanceImpl child = (ActivityInstanceImpl) childActivityInstances[i];
      child.writeTree(writer, prefix + (isTail ? "    " : "│   "), (i==(childActivityInstances.length-1)));
    }
  }

  protected void writeTransition(TransitionInstance transition, StringWriter writer, String prefix, boolean isTail) {
    writer.append(prefix);
    if(isTail) {
      writer.append("└── ");
    } else {
      writer.append("├── ");
    }

    writer.append("transition to/from " + transition.getActivityId() + ":" + transition.getId() + "\n");
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    writeTree(writer, "", true);
    return writer.toString();
  }

  public ActivityInstance[] getActivityInstances(String activityId) {
    EnsureUtil.ensureNotNull("activityId", activityId);

    List<ActivityInstance> instances = new ArrayList<ActivityInstance>();
    collectActivityInstances(activityId, instances);

    return instances.toArray(new ActivityInstance[instances.size()]);
  }

  protected void collectActivityInstances(String activityId, List<ActivityInstance> instances) {
    if (this.activityId.equals(activityId)) {
      instances.add(this);
    }
    else {
      for (ActivityInstance childInstance : childActivityInstances) {
        ((ActivityInstanceImpl) childInstance).collectActivityInstances(activityId, instances);
      }
    }
  }

  public TransitionInstance[] getTransitionInstances(String activityId) {
    EnsureUtil.ensureNotNull("activityId", activityId);

    List<TransitionInstance> instances = new ArrayList<TransitionInstance>();
    collectTransitionInstances(activityId, instances);

    return instances.toArray(new TransitionInstance[instances.size()]);
  }

  protected void collectTransitionInstances(String activityId, List<TransitionInstance> instances) {
    boolean instanceFound = false;

    for (TransitionInstance childTransitionInstance : childTransitionInstances) {
      if (activityId.equals(childTransitionInstance.getActivityId())) {
        instances.add(childTransitionInstance);
        instanceFound = true;
      }
    }

    if (!instanceFound) {
      for (ActivityInstance childActivityInstance : childActivityInstances) {
        ((ActivityInstanceImpl) childActivityInstance).collectTransitionInstances(activityId, instances);
      }
    }
  }

}
