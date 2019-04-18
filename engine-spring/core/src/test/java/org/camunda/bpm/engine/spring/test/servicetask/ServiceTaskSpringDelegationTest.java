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
package org.camunda.bpm.engine.spring.test.servicetask;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.spring.test.SpringProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Joram Barrez
 */
@ContextConfiguration("classpath:org/camunda/bpm/engine/spring/test/servicetask/servicetaskSpringTest-context.xml")
public class ServiceTaskSpringDelegationTest extends SpringProcessEngineTestCase {
  
  @Deployment
  public void testDelegateExpression() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("delegateExpressionToSpringBean");
    assertEquals("Activiti BPMN 2.0 process engine", runtimeService.getVariable(procInst.getId(), "myVar"));
    assertEquals("fieldInjectionWorking", runtimeService.getVariable(procInst.getId(), "fieldInjection"));
  }

  @Deployment
  public void testDelegateClass() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("delegateClassToSpringBean");
    assertEquals("Activiti BPMN 2.0 process engine", runtimeService.getVariable(procInst.getId(), "myVar"));
    assertEquals("fieldInjectionWorking", runtimeService.getVariable(procInst.getId(), "fieldInjection"));
  }

  @Deployment
  public void testDelegateClassNotABean() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("delegateClassToSpringBean");
    assertEquals("DelegateClassNotABean was called", runtimeService.getVariable(procInst.getId(), "message"));
    assertTrue((Boolean)runtimeService.getVariable(procInst.getId(), "injectedFieldIsNull"));
  }
  
  @Deployment
  public void testMethodExpressionOnSpringBean() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("methodExpressionOnSpringBean");
    assertEquals("ACTIVITI BPMN 2.0 PROCESS ENGINE", runtimeService.getVariable(procInst.getId(), "myVar"));
  }

  @Deployment
  public void testExecutionAndTaskListenerDelegationExpression() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionAndTaskListenerDelegation");
    assertEquals("working", runtimeService.getVariable(processInstance.getId(), "executionListenerVar"));
    assertEquals("working", runtimeService.getVariable(processInstance.getId(), "taskListenerVar"));
    
    assertEquals("executionListenerInjection", runtimeService.getVariable(processInstance.getId(), "executionListenerField"));
    assertEquals("taskListenerInjection", runtimeService.getVariable(processInstance.getId(), "taskListenerField"));
  }
  
}
