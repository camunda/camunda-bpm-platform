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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.junit.Assert;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExecutionTreeProcessDefinitionIdAssertion implements ExecutionTreeAssertion {

  protected String expectedProcessDefinitionId;

  @Override
  public void assertExecution(ExecutionTree tree) {
    List<Execution> nonMatchingExecutions = matches(tree);

    if (!nonMatchingExecutions.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      sb.append("Expected all executions to have process definition id " + expectedProcessDefinitionId + "\n");
      sb.append("Actual Tree: \n");
      sb.append(tree);
      sb.append("\nExecutions with unexpected process definition id:\n");
      sb.append("[\n");
      for (Execution execution : nonMatchingExecutions) {
        sb.append(execution);
        sb.append("\n");
      }
      sb.append("]\n");
      Assert.fail(sb.toString());
    }
  }

  /**
   * returns umatched executions in the tree
   */
  protected List<Execution> matches(ExecutionTree tree) {
    ExecutionEntity executionEntity = (ExecutionEntity) tree.getExecution();
    List<Execution> unmatchedExecutions = new ArrayList<Execution>();

    if (!expectedProcessDefinitionId.equals(executionEntity.getProcessDefinitionId())) {
      unmatchedExecutions.add(tree.getExecution());
    }
    for (ExecutionTree child : tree.getExecutions()) {
      unmatchedExecutions.addAll(matches(child));
    }

    return unmatchedExecutions;
  }

  public static ExecutionTreeProcessDefinitionIdAssertion processDefinitionId(String expectedProcessDefinitionId) {
    ExecutionTreeProcessDefinitionIdAssertion assertion = new ExecutionTreeProcessDefinitionIdAssertion();
    assertion.expectedProcessDefinitionId = expectedProcessDefinitionId;

    return assertion;
  }

}
