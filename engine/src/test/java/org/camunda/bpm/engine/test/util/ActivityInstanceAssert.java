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
import java.util.List;
import java.util.Stack;

import org.camunda.bpm.engine.impl.persistence.entity.ActivityInstanceImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;
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
    protected boolean isTreeMatched(ActivityInstance actualInstance, ActivityInstance expectedInstance) {
      if(!expectedInstance.getActivityId().equals(actualInstance.getActivityId())) {
        return false;
      } else {
        if(expectedInstance.getChildActivityInstances().length != actualInstance.getChildActivityInstances().length) {
          return false;
        } else {

          List<ActivityInstance> unmatchedInstances = new ArrayList<ActivityInstance>(Arrays.asList(expectedInstance.getChildActivityInstances()));
          for (ActivityInstance child1 : actualInstance.getChildActivityInstances()) {
            boolean matchFound = false;
            for (ActivityInstance child2 : new ArrayList<ActivityInstance>(unmatchedInstances)) {
              if (isTreeMatched(child1, child2)) {
                unmatchedInstances.remove(child2);
                matchFound = true;
                break;
              }
            }
            if(!matchFound) {
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
      ActivityInstanceImpl newInstance = new ActivityInstanceImpl();
      newInstance.setActivityId(activityId);

      ActivityInstanceImpl parentInstance = activityInstanceStack.peek();
      List<ActivityInstance> childInstances = new ArrayList<ActivityInstance>(Arrays.asList(parentInstance.getChildActivityInstances()));
      childInstances.add(newInstance);
      parentInstance.setChildActivityInstances(childInstances.toArray(new ActivityInstance[childInstances.size()]));

      activityInstanceStack.push(newInstance);

      return this;
    }

    public ActivityInstanceTreeBuilder activity(String activityId) {

      beginScope(activityId);
      endScope();

      return this;
    }

    public ActivityInstanceTreeBuilder endScope() {
      activityInstanceStack.pop();
      return this;
    }

    public ActivityInstance done() {
      return rootInstance;
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
