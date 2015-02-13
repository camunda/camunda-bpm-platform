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

  public void matches(ExecutionTreeAssertion assertion) {
    assertion.assertExecution(tree);
  }

  public static class ExecutionTreeBuilder {

    protected ExecutionTreeAssertion rootAssertion = null;
    protected Stack<ExecutionTreeAssertion> activityInstanceStack = new Stack<ExecutionTreeAssertion>();

    public ExecutionTreeBuilder(String rootActivityInstanceId) {
      rootAssertion = new ExecutionTreeAssertion();
      rootAssertion.setExpectedActivityId(rootActivityInstanceId);
      activityInstanceStack.push(rootAssertion);
    }

    public ExecutionTreeBuilder child(String activityId) {
      ExecutionTreeAssertion newInstance = new ExecutionTreeAssertion();
      newInstance.setExpectedActivityId(activityId);

      ExecutionTreeAssertion parentInstance = activityInstanceStack.peek();
      parentInstance.addChildAssertion(newInstance);

      activityInstanceStack.push(newInstance);

      return this;
    }

    public ExecutionTreeBuilder scope() {
      ExecutionTreeAssertion currentAssertion = activityInstanceStack.peek();
      currentAssertion.setExpectedIsScope(true);
      return this;
    }

    public ExecutionTreeBuilder concurrent() {
      ExecutionTreeAssertion currentAssertion = activityInstanceStack.peek();
      currentAssertion.setExpectedIsConcurrent(true);
      return this;
    }

    public ExecutionTreeBuilder noScope() {
      ExecutionTreeAssertion currentAssertion = activityInstanceStack.peek();
      currentAssertion.setExpectedIsScope(false);
      return this;
    }

    public ExecutionTreeBuilder up() {
      activityInstanceStack.pop();
      return this;
    }

    public ExecutionTreeAssertion done() {
      return rootAssertion;
    }
  }

  public static ExecutionTreeBuilder describeExecutionTree(String activityId) {
    return new ExecutionTreeBuilder(activityId);
  }

}
