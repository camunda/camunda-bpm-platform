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
package org.camunda.bpm.engine.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ActivityInstanceImpl;
import org.camunda.bpm.engine.impl.persistence.entity.TransitionInstanceImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.TransitionInstance;
import org.junit.Assert;

/**
 * @author Daniel Meyer
 *
 */
public class ActivityInstanceAssert {

  public static class ActivityInstanceAssertThatClause {

    protected ActivityInstance actual;

    public ActivityInstanceAssertThatClause(ActivityInstance actual) {
      this.actual = actual;
    }

    public void hasStructure(ActivityInstance expected) {
      assertTreeMatch(expected, actual);
    }

    protected void assertTreeMatch(ActivityInstance expected, ActivityInstance actual) {
      boolean treesMatch = isTreeMatched(expected, actual);
      if (!treesMatch) {
        Assert.fail("Could not match expected tree \n" + expected +" \n\n with actual tree \n\n "+actual);
      }

    }


    /** if anyone wants to improve this algorithm, feel welcome! */
    protected boolean isTreeMatched(ActivityInstance expectedInstance, ActivityInstance actualInstance) {
      if(!expectedInstance.getActivityId().equals(actualInstance.getActivityId())
          || (expectedInstance.getId() != null && !expectedInstance.getId().equals(actualInstance.getId()))) {
        return false;
      } else {
        if(expectedInstance.getChildActivityInstances().length != actualInstance.getChildActivityInstances().length) {
          return false;
        } else {

          List<ActivityInstance> unmatchedInstances = new ArrayList<ActivityInstance>(Arrays.asList(expectedInstance.getChildActivityInstances()));
          for (ActivityInstance actualChild : actualInstance.getChildActivityInstances()) {
            boolean matchFound = false;
            for (ActivityInstance expectedChild : new ArrayList<ActivityInstance>(unmatchedInstances)) {
              if (isTreeMatched(expectedChild, actualChild)) {
                unmatchedInstances.remove(actualChild);
                matchFound = true;
                break;
              }
            }
            if(!matchFound) {
              return false;
            }
          }

          if (expectedInstance.getChildTransitionInstances().length != actualInstance.getChildTransitionInstances().length) {
            return false;
          }

          List<TransitionInstance> unmatchedTransitionInstances =
              new ArrayList<TransitionInstance>(Arrays.asList(expectedInstance.getChildTransitionInstances()));
          for (TransitionInstance child : actualInstance.getChildTransitionInstances()) {
            Iterator<TransitionInstance> expectedTransitionInstanceIt = unmatchedTransitionInstances.iterator();

            boolean matchFound = false;
            while (expectedTransitionInstanceIt.hasNext() && !matchFound) {
              TransitionInstance expectedChild = expectedTransitionInstanceIt.next();
              if (expectedChild.getActivityId().equals(child.getActivityId())) {
                matchFound = true;
                expectedTransitionInstanceIt.remove();
              }
            }

            if (!matchFound) {
              return false;
            }
          }

        }
        return true;

      }
    }

  }

  public static class ActivityInstanceTreeBuilder {

    protected ActivityInstanceImpl rootInstance = null;
    protected Stack<ActivityInstanceImpl> activityInstanceStack = new Stack<ActivityInstanceImpl>();

    public ActivityInstanceTreeBuilder() {
      this(null);
    }

    public ActivityInstanceTreeBuilder(String rootActivityId) {
      rootInstance = new ActivityInstanceImpl();
      rootInstance.setActivityId(rootActivityId);
      activityInstanceStack.push(rootInstance);
    }

    public ActivityInstanceTreeBuilder beginScope(String activityId) {
      return beginScope(activityId, null);
    }

    public ActivityInstanceTreeBuilder beginScope(String activityId, String activityInstanceId) {
      ActivityInstanceImpl newInstance = new ActivityInstanceImpl();
      newInstance.setActivityId(activityId);
      newInstance.setId(activityInstanceId);

      ActivityInstanceImpl parentInstance = activityInstanceStack.peek();
      List<ActivityInstance> childInstances = new ArrayList<ActivityInstance>(Arrays.asList(parentInstance.getChildActivityInstances()));
      childInstances.add(newInstance);
      parentInstance.setChildActivityInstances(childInstances.toArray(new ActivityInstance[childInstances.size()]));

      activityInstanceStack.push(newInstance);

      return this;
    }

    public ActivityInstanceTreeBuilder beginMiBody(String activityId) {
      return beginScope(activityId + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, null);
    }

    public ActivityInstanceTreeBuilder beginMiBody(String activityId, String activityInstanceId) {
      return beginScope(activityId + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, activityInstanceId);
    }

    public ActivityInstanceTreeBuilder activity(String activityId) {

      return activity(activityId, null);
    }

    public ActivityInstanceTreeBuilder activity(String activityId, String activityInstanceId) {

      beginScope(activityId);
      id(activityInstanceId);
      endScope();

      return this;
    }

    public ActivityInstanceTreeBuilder transition(String activityId) {

      TransitionInstanceImpl newInstance = new TransitionInstanceImpl();
      newInstance.setActivityId(activityId);
      ActivityInstanceImpl parentInstance = activityInstanceStack.peek();

      List<TransitionInstance> childInstances = new ArrayList<TransitionInstance>(
          Arrays.asList(parentInstance.getChildTransitionInstances()));
      childInstances.add(newInstance);
      parentInstance.setChildTransitionInstances(childInstances.toArray(new TransitionInstance[childInstances.size()]));

      return this;
    }

    public ActivityInstanceTreeBuilder endScope() {
      activityInstanceStack.pop();
      return this;
    }

    public ActivityInstance done() {
      return rootInstance;
    }

    protected ActivityInstanceTreeBuilder id(String expectedActivityInstanceId) {
      ActivityInstanceImpl activityInstanceImpl = activityInstanceStack.peek();
      activityInstanceImpl.setId(expectedActivityInstanceId);
      return this;
    }
  }

  public static ActivityInstanceTreeBuilder describeActivityInstanceTree() {
    return new ActivityInstanceTreeBuilder();
  }

  public static ActivityInstanceTreeBuilder describeActivityInstanceTree(String rootActivityId) {
    return new ActivityInstanceTreeBuilder(rootActivityId);
  }

  public static ActivityInstanceAssertThatClause assertThat(ActivityInstance actual) {
    return new ActivityInstanceAssertThatClause(actual);
  }

}
