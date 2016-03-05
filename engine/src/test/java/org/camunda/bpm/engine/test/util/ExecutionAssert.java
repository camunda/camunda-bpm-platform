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

import java.util.Stack;

import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExecutionAssert {

  protected ExecutionTree tree;
  protected CommandExecutor commandExecutor;

  public static ExecutionAssert assertThat(ExecutionTree tree) {

    ExecutionAssert assertion = new ExecutionAssert();
    assertion.tree = tree;
    return assertion;
  }

  public ExecutionAssert matches(ExecutionTreeAssertion assertion) {
    assertion.assertExecution(tree);
    return this;
  }

  public ExecutionAssert hasProcessDefinitionId(String expectedProcessDefinitionId) {
    ExecutionTreeAssertion assertion = ExecutionTreeProcessDefinitionIdAssertion.processDefinitionId(expectedProcessDefinitionId);
    matches(assertion);
    return this;
  }

  public static class ExecutionTreeBuilder {

    protected ExecutionTreeStructureAssertion rootAssertion = null;
    protected Stack<ExecutionTreeStructureAssertion> activityInstanceStack = new Stack<ExecutionTreeStructureAssertion>();

    public ExecutionTreeBuilder(String rootActivityInstanceId) {
      rootAssertion = new ExecutionTreeStructureAssertion();
      rootAssertion.setExpectedActivityId(rootActivityInstanceId);
      activityInstanceStack.push(rootAssertion);
    }

    public ExecutionTreeBuilder child(String activityId) {
      ExecutionTreeStructureAssertion newInstance = new ExecutionTreeStructureAssertion();
      newInstance.setExpectedActivityId(activityId);

      ExecutionTreeStructureAssertion parentInstance = activityInstanceStack.peek();
      parentInstance.addChildAssertion(newInstance);

      activityInstanceStack.push(newInstance);

      return this;
    }

    public ExecutionTreeBuilder scope() {
      ExecutionTreeStructureAssertion currentAssertion = activityInstanceStack.peek();
      currentAssertion.setExpectedIsScope(true);
      return this;
    }

    public ExecutionTreeBuilder concurrent() {
      ExecutionTreeStructureAssertion currentAssertion = activityInstanceStack.peek();
      currentAssertion.setExpectedIsConcurrent(true);
      return this;
    }

    public ExecutionTreeBuilder eventScope() {
      ExecutionTreeStructureAssertion currentAssertion = activityInstanceStack.peek();
      currentAssertion.setExpectedIsEventScope(true);
      return this;
    }

    public ExecutionTreeBuilder noScope() {
      ExecutionTreeStructureAssertion currentAssertion = activityInstanceStack.peek();
      currentAssertion.setExpectedIsScope(false);
      return this;
    }

    public ExecutionTreeBuilder id(String id) {
      ExecutionTreeStructureAssertion currentAssertion = activityInstanceStack.peek();
      currentAssertion.setExpectedId(id);
      return this;
    }

    public ExecutionTreeBuilder up() {
      activityInstanceStack.pop();
      return this;
    }

    public ExecutionTreeStructureAssertion done() {
      return rootAssertion;
    }
  }

  public static ExecutionTreeBuilder describeExecutionTree(String activityId) {
    return new ExecutionTreeBuilder(activityId);
  }

}
