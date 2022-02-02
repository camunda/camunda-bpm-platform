/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.upgrade.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
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

    public void hasStructure(ExpectedActivityInstance expected) {
      assertTreeMatch(expected, actual);
    }

    protected void assertTreeMatch(ExpectedActivityInstance expected, ActivityInstance actual) {
      boolean treesMatch = isTreeMatched(expected, actual);
      if (!treesMatch) {
        Assert.fail("Could not match expected tree \n" + expected +" \n\n with actual tree \n\n "+actual);
      }

    }


    /** if anyone wants to improve this algorithm, feel welcome! */
    protected boolean isTreeMatched(ExpectedActivityInstance expectedInstance, ActivityInstance actualInstance) {
      if(!expectedInstance.getActivityIds().contains(actualInstance.getActivityId())) {
        return false;
      } else {
        if(expectedInstance.getChildActivityInstances().size() != actualInstance.getChildActivityInstances().length) {
          return false;
        } else {

          List<ExpectedActivityInstance> unmatchedInstances = new ArrayList<>(expectedInstance.getChildActivityInstances());
          for (ActivityInstance child1 : actualInstance.getChildActivityInstances()) {
            boolean matchFound = false;
            for (ExpectedActivityInstance child2 : new ArrayList<>(unmatchedInstances)) {
              if (isTreeMatched(child2, child1)) {
                unmatchedInstances.remove(child2);
                matchFound = true;
                break;
              }
            }
            if(!matchFound) {
              return false;
            }
          }

          List<ExpectedTransitionInstance> unmatchedTransitionInstances =
              new ArrayList<>(expectedInstance.getChildTransitionInstances());
          for (TransitionInstance child : actualInstance.getChildTransitionInstances()) {
            Iterator<ExpectedTransitionInstance> expectedTransitionInstanceIt = unmatchedTransitionInstances.iterator();

            boolean matchFound = false;
            while (expectedTransitionInstanceIt.hasNext() && !matchFound) {
              ExpectedTransitionInstance expectedChild = expectedTransitionInstanceIt.next();
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

    protected ExpectedActivityInstance rootInstance = null;
    protected Stack<ExpectedActivityInstance> activityInstanceStack = new Stack<>();

    public ActivityInstanceTreeBuilder() {
      this(null);
    }

    public ActivityInstanceTreeBuilder(String rootActivityId) {
      rootInstance = new ExpectedActivityInstance();
      rootInstance.setActivityId(rootActivityId);
      activityInstanceStack.push(rootInstance);
    }

    public ActivityInstanceTreeBuilder beginScope(String... activityIds) {
      ExpectedActivityInstance newInstance = new ExpectedActivityInstance();
      newInstance.setActivityIds(activityIds);

      ExpectedActivityInstance parentInstance = activityInstanceStack.peek();
      List<ExpectedActivityInstance> childInstances = new ArrayList<>(parentInstance.getChildActivityInstances());
      childInstances.add(newInstance);
      parentInstance.setChildActivityInstances(childInstances);

      activityInstanceStack.push(newInstance);

      return this;
    }

    public ActivityInstanceTreeBuilder beginMiBody(String activityId) {
      return beginScope(activityId + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX);
    }

    public ActivityInstanceTreeBuilder activity(String activityId) {

      beginScope(activityId);
      endScope();

      return this;
    }

    public ActivityInstanceTreeBuilder transition(String activityId) {

      ExpectedTransitionInstance newInstance = new ExpectedTransitionInstance();
      newInstance.setActivityId(activityId);
      ExpectedActivityInstance parentInstance = activityInstanceStack.peek();

      List<ExpectedTransitionInstance> childInstances = new ArrayList<>(
          parentInstance.getChildTransitionInstances());
      childInstances.add(newInstance);
      parentInstance.setChildTransitionInstances(childInstances);

      return this;
    }

    public ActivityInstanceTreeBuilder endScope() {
      activityInstanceStack.pop();
      return this;
    }

    public ExpectedActivityInstance done() {
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
