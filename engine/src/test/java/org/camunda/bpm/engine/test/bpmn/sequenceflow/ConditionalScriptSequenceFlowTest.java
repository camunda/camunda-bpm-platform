/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.test.bpmn.sequenceflow;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Sebastian Menski
 */
public class ConditionalScriptSequenceFlowTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testScriptExpression() {
    String[] directions = new String[] { "left", "right" };
    Map<String, Object> variables = new HashMap<String, Object>();

    for (String direction : directions) {
      variables.put("foo", direction);
      runtimeService.startProcessInstanceByKey("process", variables);

      Task task = taskService.createTaskQuery().singleResult();
      assertEquals(direction, task.getTaskDefinitionKey());
      taskService.complete(task.getId());
    }

  }

  @Deployment
  public void testScriptExpressionWithNonBooleanResult() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("expected exception: invalid return value in script");
    } catch (ProcessEngineException e) {
      assertTextPresent("condition script returns non-Boolean", e.getMessage());
    }
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/sequenceflow/ConditionalScriptSequenceFlowTest.testScriptResourceExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/sequenceflow/condition-left.groovy"
  })
  public void testScriptResourceExpression() {
    String[] directions = new String[] { "left", "right" };
    Map<String, Object> variables = new HashMap<String, Object>();

    for (String direction : directions) {
      variables.put("foo", direction);
      runtimeService.startProcessInstanceByKey("process", variables);

      Task task = taskService.createTaskQuery().singleResult();
      assertEquals(direction, task.getTaskDefinitionKey());
      taskService.complete(task.getId());
    }

  }

}
