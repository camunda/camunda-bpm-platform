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
package org.camunda.bpm.engine.test.bpmn.event.end;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Kristin Polenz
 * @author Nico Rehwaldt
 */
public class MessageEndEventTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testMessageEndEvent() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    assertNotNull(processInstance);
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Deployment
  @Test
  public void testMessageEndEventServiceTaskBehavior() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();

    // class
    variables.put("wasExecuted", true);
    variables.put("expressionWasExecuted", false);
    variables.put("delegateExpressionWasExecuted", false);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);
    assertNotNull(processInstance);

    testRule.assertProcessEnded(processInstance.getId());
    assertTrue(DummyServiceTask.wasExecuted);

    // expression
    variables = new HashMap<String, Object>();
    variables.put("wasExecuted", false);
    variables.put("expressionWasExecuted", true);
    variables.put("delegateExpressionWasExecuted", false);
    variables.put("endEventBean", new EndEventBean());
    processInstance = runtimeService.startProcessInstanceByKey("process", variables);
    assertNotNull(processInstance);

    testRule.assertProcessEnded(processInstance.getId());
    assertTrue(DummyServiceTask.expressionWasExecuted);

    // delegate expression
    variables = new HashMap<String, Object>();
    variables.put("wasExecuted", false);
    variables.put("expressionWasExecuted", false);
    variables.put("delegateExpressionWasExecuted", true);
    variables.put("endEventBean", new EndEventBean());
    processInstance = runtimeService.startProcessInstanceByKey("process", variables);
    assertNotNull(processInstance);

    testRule.assertProcessEnded(processInstance.getId());
    assertTrue(DummyServiceTask.delegateExpressionWasExecuted);
  }

}
