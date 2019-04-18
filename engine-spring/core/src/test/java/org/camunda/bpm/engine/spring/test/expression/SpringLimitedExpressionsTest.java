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
package org.camunda.bpm.engine.spring.test.expression;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.spring.test.SpringProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;


/**
 * Test limiting the exposed beans in expressions.
 * 
 * @author Frederik Heremans
 */
@ContextConfiguration("classpath:org/camunda/bpm/engine/spring/test/expression/expressionLimitedBeans-context.xml")
public class SpringLimitedExpressionsTest extends SpringProcessEngineTestCase {

  @Deployment
  public void testLimitedBeansExposed() throws Exception {
    // Start process, which has a service-task which calls 'bean1', which is exposed
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("limitedExpressionProcess");
    
    String beanOutput = (String) runtimeService.getVariable(processInstance.getId(), "beanOutput");
    assertNotNull(beanOutput);
    assertEquals("Activiti BPMN 2.0 process engine", beanOutput);
    
    // Finish the task, should continue to serviceTask which uses a bean that is present
    // in application-context, but not exposed explicitly in "beans", should throw error!
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    try {
      taskService.complete(task.getId());
      fail("Exception should have been thrown");
    } catch(ProcessEngineException ae) {
      assertTextPresent("Unknown property used in expression", ae.getMessage());
    }
  }
}
