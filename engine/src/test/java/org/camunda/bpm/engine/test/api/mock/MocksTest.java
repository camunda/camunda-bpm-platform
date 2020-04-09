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
package org.camunda.bpm.engine.test.api.mock;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

import java.util.HashMap;

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
  public void testMockAvailabilityInScriptTask() {
    testMockAvailability();
  }

  @Test
  @Deployment
  public void testMockAvailabilityInExpressionLanguage() {
    testMockAvailability();
  }

  //helper ////////////////////////////////////////////////////////////
  private void testMockAvailability() {
    //given
    final String testStr = "testValue";

    Mocks.register("myMock", new Object() {

      public String getTest() {
        return testStr;
      }

      public void testMethod(DelegateExecution execution, String str) {
        execution.setVariable("testVar", str);
      }

    });

    //when
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("mocksTest");
    Mocks.reset();

    //then
    assertEquals(testStr, runtimeService.getVariable(pi.getId(), "testVar"));
  }

}
