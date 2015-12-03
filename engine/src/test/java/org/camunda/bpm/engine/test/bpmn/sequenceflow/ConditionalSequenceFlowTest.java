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

package org.camunda.bpm.engine.test.bpmn.sequenceflow;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;


/**
 * @author Joram Barrez
 * @author Falko Menge (camunda)
 */
public class ConditionalSequenceFlowTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testUelExpression() {
    Map<String, Object> variables = CollectionUtil.singletonMap("input", "right");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("condSeqFlowUelExpr", variables);

    Task task = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .singleResult();

    assertNotNull(task);
    assertEquals("task right", task.getName());
  }

  @Deployment
  public void testValueAndMethodExpression() {
    // An order of price 150 is a standard order (goes through an UEL value expression)
    ConditionalSequenceFlowTestOrder order = new ConditionalSequenceFlowTestOrder(150);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("uelExpressions",
            CollectionUtil.singletonMap("order",  order));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("Standard service", task.getName());

    // While an order of 300, gives us a premium service (goes through an UEL method expression)
    order = new ConditionalSequenceFlowTestOrder(300);
    processInstance = runtimeService.startProcessInstanceByKey("uelExpressions",
            CollectionUtil.singletonMap("order",  order));
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("Premium service", task.getName());

  }

  /**
   * Test that Conditional Sequence Flows throw an exception, if no condition
   * evaluates to true.
   *
   * BPMN 2.0.1 p. 427 (PDF 457):
   * "Multiple outgoing Sequence Flows with conditions behaves as an inclusive split."
   *
   * BPMN 2.0.1 p. 436 (PDF 466):
   * "The inclusive gateway throws an exception in case all conditions evaluate to false and a default flow has not been specified."
   *
   * @see https://app.camunda.com/jira/browse/CAM-1773
   */
  @Deployment
  public void testNoExpressionTrueThrowsException() {
    Map<String, Object> variables = CollectionUtil.singletonMap("input", "non-existing-value");
    try {
      runtimeService.startProcessInstanceByKey("condSeqFlowUelExpr", variables);
      fail("Expected ProcessEngineException");
    } catch (ProcessEngineException e) {
      assertTextPresent("No conditional sequence flow leaving the Flow Node 'theStart' could be selected for continuing the process", e.getMessage());
    }
  }

}
