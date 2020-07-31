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
package org.camunda.bpm.engine.test.standalone.pvm;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.camunda.bpm.engine.impl.pvm.PvmExecution;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.PvmProcessInstance;
import org.camunda.bpm.engine.test.standalone.pvm.activities.WaitState;
import org.junit.Test;


/**
 * @author Tom Baeyens
 */
public class PvmVariablesTest {

  @Test
  public void testVariables() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("a")
        .initial()
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();
      
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.setVariable("amount", 500L);
    processInstance.setVariable("msg", "hello world");
    processInstance.start();

    assertEquals(500L, processInstance.getVariable("amount"));
    assertEquals("hello world", processInstance.getVariable("msg"));

    PvmExecution activityInstance = processInstance.findExecution("a");
    assertEquals(500L, activityInstance.getVariable("amount"));
    assertEquals("hello world", activityInstance.getVariable("msg"));
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("amount", 500L);
    expectedVariables.put("msg", "hello world");

    assertEquals(expectedVariables, activityInstance.getVariables());
    assertEquals(expectedVariables, processInstance.getVariables());
  }
}