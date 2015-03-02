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
import java.util.List;

import org.junit.Assert;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExecutionTreeAssertion {

  protected String expectedActivityId;
  protected Boolean expectedIsScope = true;
  protected Boolean expectedIsConcurrent = false;
  protected Boolean expectedIsEventScope = false;

  protected List<ExecutionTreeAssertion> childAssertions = new ArrayList<ExecutionTreeAssertion>();

  public void addChildAssertion(ExecutionTreeAssertion childAssertion) {
    this.childAssertions.add(childAssertion);
  }

  public void setExpectedActivityId(String expectedActivityId) {
    this.expectedActivityId = expectedActivityId;
  }

  /**
   * This assumes that all children have been fetched
   */
  protected boolean matches(ExecutionTree tree) {
    // match activity id
    String actualActivityId = tree.getActivityId();
    if (expectedActivityId == null && actualActivityId != null) {
      return false;
    } else if (expectedActivityId != null && !expectedActivityId.equals(tree.getActivityId())) {
      return false;
    }


    // match is scope
    if (expectedIsScope != null && !expectedIsScope.equals(tree.isScope())) {
      return false;
    }

    if (expectedIsConcurrent != null && !expectedIsConcurrent.equals(tree.isConcurrent())) {
      return false;
    }

    if (expectedIsEventScope != null && !expectedIsEventScope.equals(tree.isEventScope())) {
      return false;
    }

    // match children
    if (tree.getExecutions().size() != childAssertions.size()) {
      return false;
    }

    List<ExecutionTreeAssertion> unmatchedChildAssertions = new ArrayList<ExecutionTreeAssertion>(childAssertions);
    for (ExecutionTree child : tree.getExecutions()) {
      for (ExecutionTreeAssertion childAssertion : unmatchedChildAssertions) {
        if (childAssertion.matches(child)) {
          unmatchedChildAssertions.remove(childAssertion);
          break;
        }
      }
    }

    if (!unmatchedChildAssertions.isEmpty()) {
      return false;
    }

    return true;
  }

  public void assertExecution(ExecutionTree tree) {
    boolean matches = matches(tree);
    if (!matches) {
      StringBuilder errorBuilder = new StringBuilder();
      errorBuilder.append("Expected tree: \n");
      describe(this, "", errorBuilder);
      errorBuilder.append("Actual tree: \n");
      describe(tree, "", errorBuilder);
      Assert.fail(errorBuilder.toString());
    }
  }

  public static void describe(ExecutionTree tree, String prefix, StringBuilder errorBuilder) {
    errorBuilder.append(prefix);
    errorBuilder.append(executionTreeToString(tree));
    errorBuilder.append("\n");
    for (ExecutionTree child : tree.getExecutions()) {
      describe(child, prefix + "   ", errorBuilder);
    }
  }

  public static void describe(ExecutionTreeAssertion assertion, String prefix, StringBuilder errorBuilder) {
    errorBuilder.append(prefix);
    errorBuilder.append(assertion);
    errorBuilder.append("\n");
    for (ExecutionTreeAssertion child : assertion.childAssertions) {
      describe(child, prefix + "   ", errorBuilder);
    }
  }

  public static String executionTreeToString(ExecutionTree executionTree) {
    StringBuilder sb = new StringBuilder();
    sb.append(executionTree.getExecution());

    sb.append("[activityId=");
    sb.append(executionTree.getActivityId());

    sb.append(", isScope=");
    sb.append(executionTree.isScope());

    sb.append(", isConcurrent=");
    sb.append(executionTree.isConcurrent());

    sb.append(", isEventScope=");
    sb.append(executionTree.isEventScope());

    sb.append("]");

    return sb.toString();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[activityId=");
    sb.append(expectedActivityId);

    if (expectedIsScope != null) {
      sb.append(", isScope=");
      sb.append(expectedIsScope);
    }

    if (expectedIsConcurrent != null) {
      sb.append(", isConcurrent=");
      sb.append(expectedIsConcurrent);
    }

    if (expectedIsEventScope != null) {
      sb.append(", isEventScope=");
      sb.append(expectedIsEventScope);
    }

    sb.append("]");

    return sb.toString();
  }

  public void setExpectedIsScope(Boolean expectedIsScope) {
    this.expectedIsScope = expectedIsScope;

  }

  public void setExpectedIsConcurrent(Boolean expectedIsConcurrent) {
    this.expectedIsConcurrent = expectedIsConcurrent;
  }

  public void setExpectedIsEventScope(Boolean expectedIsEventScope) {
    this.expectedIsEventScope = expectedIsEventScope;
  }

}
