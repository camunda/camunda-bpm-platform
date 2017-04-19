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
package org.camunda.bpm.engine.test.api.mock;

import java.util.HashMap;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * @author Tassilo Weidner
 */
public class MocksTest {
  @Rule
  public ProcessEngineRule rule = new ProvidedProcessEngineRule();

  protected RuntimeService runtimeService;
  protected TaskService taskService;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
  }

  @Test
  public void testMethodsOfMocksAPI() {
    //given
    HashMap<String, Object> map = new HashMap<String, Object>();

    for (int i = 0; i < 5; i++) {
      map.put("key" + i, new Object());
    }

    //when
    for (String key : map.keySet()) {
      Mocks.register(key, map.get(key));
    }

    //then
    for (String key : map.keySet()) {
      assertEquals(map.get(key), Mocks.get(key));
    }

    assertEquals(map, Mocks.getMocks());

    Mocks.reset();

    for (String key : map.keySet()) {
      assertNull(Mocks.get(key));
    }

    assertEquals(0, Mocks.getMocks().size());
  }

  @Test
  @Deployment
  public void testUnknownMethodInCamundaExpressionAppliedOnServiceTask() {
    //given
    Mocks.register("myMock", new Object() {});

    try {
      //when
      runtimeService.startProcessInstanceByKey("mocksTest");
      fail();
    } catch (ProcessEngineException e) {
      //then
      assertTrue(e.getMessage().contains("Unknown method used in expression: #{myMock.testMethod()}."));
    } finally {
      Mocks.reset();
    }
  }

  @Test
  @Deployment
  public void testCamundaExpressionAppliedOnServiceTask() {
    //given
    Mocks.register("myMock", new Object() {
      public String getTest() {
        return "testValue";
      }

      public void testMethod(DelegateExecution execution, String str) {
        execution.setVariable("testVar", str);
      }
    });

    //when
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("mocksTest");
    runtimeService.setVariable(pi.getId(), "myVar", 42);
    Mocks.reset();

    //then
    assertEquals(42, runtimeService.getVariable(pi.getId(), "myVar"));
    assertEquals("testValue", runtimeService.getVariable(pi.getId(), "testVar"));
  }

  @Test
  @Deployment
  public void testConditionExpressionAppliedOnExclusiveGateway() {
    //given
    TaskChooser taskChooser = new TaskChooser();
    Mocks.register("myMock", taskChooser);

    //when
    taskChooser.setTask("red");
    ProcessInstance piWithRedTaskChosen = runtimeService.startProcessInstanceByKey("mocksTest");

    taskChooser.setTask("yellow");
    ProcessInstance piWithYellowTaskChosen = runtimeService.startProcessInstanceByKey("mocksTest");

    taskChooser.setTask("green");
    ProcessInstance piWithGreenTaskChosen = runtimeService.startProcessInstanceByKey("mocksTest");

    Mocks.reset();

    //then
    assertEquals("A Red Task", getTaskName(piWithRedTaskChosen.getId()));
    assertEquals("A Yellow Task", getTaskName(piWithYellowTaskChosen.getId()));
    assertEquals("A Green Task", getTaskName(piWithGreenTaskChosen.getId()));
  }

  //helper ////////////////////////////////////////////////////////////
  private String getTaskName(String id) {
    return taskService
            .createTaskQuery()
            .processInstanceId(id)
            .singleResult()
            .getName();
  }

  class TaskChooser {
    private String taskName = null;

    public String getTask() {
      return taskName;
    }

    private void setTask(String taskName) {
      this.taskName = taskName;
    }
  }

}
