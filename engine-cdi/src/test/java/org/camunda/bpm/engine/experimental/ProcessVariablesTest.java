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
package org.camunda.bpm.engine.experimental;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ProcessVariablesTest extends CdiProcessEngineTestCase {

  @Ignore
  @Test
  @Deployment(resources = "org/activiti/cdi/BusinessProcessBeanTest.test.bpmn20.xml")
  public void testResolveString() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    Map<String, Object> processVariables = new HashMap<String, Object>();
    businessProcess.setVariable("testKeyString", "testValue");
    businessProcess.startProcessByKey("businessProcessBeanTest", processVariables);    
    businessProcess.startTask(taskService.createTaskQuery().singleResult().getId());
    
    InjectProcessVariable injectProcessVariables = getBeanInstance(InjectProcessVariable.class);
    assertEquals("testValue", injectProcessVariables.testKeyString);

    businessProcess.completeTask();
  }

}
