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
package org.camunda.bpm.engine.test.bpmn.iomapping;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class InputOutputEventTest extends PluggableProcessEngineTestCase {

  protected void setUp() throws Exception {
    super.setUp();

    VariableLogDelegate.reset();
  }


  @Deployment
  public void testMessageThrowEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    // input mapping
    Map<String, Object> mappedVariables = VariableLogDelegate.LOCAL_VARIABLES;
    assertEquals(1, mappedVariables.size());
    assertEquals("mappedValue", mappedVariables.get("mappedVariable"));

    // output mapping
    String variable = (String) runtimeService.getVariableLocal(processInstance.getId(), "outVariable");
    assertEquals("mappedValue", variable);
  }

  @Deployment
  public void testMessageCatchEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    Execution messageExecution = runtimeService.createExecutionQuery().activityId("messageCatch").singleResult();

    Map<String, Object> localVariables = runtimeService.getVariablesLocal(messageExecution.getId());
    assertEquals(1, localVariables.size());
    assertEquals("mappedValue", localVariables.get("mappedVariable"));

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("messageVariable", "outValue");
    runtimeService.messageEventReceived("IncomingMessage", messageExecution.getId(), variables);

    // output mapping
    String variable = (String) runtimeService.getVariableLocal(processInstance.getId(), "outVariable");
    assertEquals("outValue", variable);
  }

  @Deployment
  public void testTimerCatchEvent() {
    Map<String, Object> variables = new HashMap<String, Object>();
    Date dueDate = DateTimeUtil.now().plusMinutes(5).toDate();
    variables.put("outerVariable", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dueDate));
    runtimeService.startProcessInstanceByKey("testProcess", variables);

    Job job = managementService.createJobQuery().singleResult();
    TimerEntity timer = (TimerEntity) job;
    assertDateEquals(dueDate, timer.getDuedate());
  }

  protected void assertDateEquals(Date expected, Date actual) {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    assertEquals(format.format(expected), format.format(actual));
  }

  @Deployment
  public void testNoneThrowEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");

    Map<String, Object> mappedVariables = VariableLogDelegate.LOCAL_VARIABLES;
    assertEquals(1, mappedVariables.size());
    assertEquals("mappedValue", mappedVariables.get("mappedVariable"));

    // output mapping
    String variable = (String) runtimeService.getVariableLocal(processInstance.getId(), "outVariable");
    assertEquals("mappedValue", variable);
  }

  public void testMessageStartEvent() {

    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputEventTest.testMessageStartEvent.bpmn20.xml")
        .deploy();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("camunda:inputOutput mapping unsupported for element type 'startEvent'", e.getMessage());
    }
  }

  public void testNoneEndEvent() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputEventTest.testNoneEndEvent.bpmn20.xml")
        .deploy();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("camunda:outputParameter not allowed for element type 'endEvent'", e.getMessage());
    }
  }

  @Deployment
  public void testMessageEndEvent() {
    runtimeService.startProcessInstanceByKey("testProcess");

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // input mapping
    Map<String, Object> mappedVariables = VariableLogDelegate.LOCAL_VARIABLES;
    assertEquals(1, mappedVariables.size());
    assertEquals("mappedValue", mappedVariables.get("mappedVariable"));
  }

  public void testMessageBoundaryEvent() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputEventTest.testMessageBoundaryEvent.bpmn20.xml")
        .deploy();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("camunda:inputOutput mapping unsupported for element type 'boundaryEvent'", e.getMessage());
    }
  }

  protected void tearDown() throws Exception {
    super.tearDown();

    VariableLogDelegate.reset();
  }

}
