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
package org.camunda.bpm.engine.spring.test.jpa;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.spring.test.SpringProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;


/**
 * Test using spring-orm in spring-bean combined with JPA-variables in activiti.
 * 
 * @author Frederik Heremans
 */
@ContextConfiguration("classpath:org/camunda/bpm/engine/spring/test/jpa/JPASpringTest-context.xml")
public class JPASpringTest extends SpringProcessEngineTestCase {
  
  @Deployment(resources = {"org/camunda/bpm/engine/spring/test/jpa/JPASpringTest.bpmn20.xml"})
  public void testJpaVariableHappyPath() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("customerName", "John Doe");
    variables.put("amount", 15000L);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("LoanRequestProcess", variables);
    
    // Variable should be present containing the loanRequest created by the spring bean
    Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
    assertNotNull(value);
    assertTrue(value instanceof LoanRequest);
    LoanRequest request = (LoanRequest) value;
    assertEquals("John Doe", request.getCustomerName());
    assertEquals(15000L, request.getAmount().longValue());
    assertFalse(request.isApproved());
    
    // We will approve the request, which will update the entity
    variables = new HashMap<String, Object>();
    variables.put("approvedByManager", Boolean.TRUE);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId(), variables);
    
    // If approved, the processsInstance should be finished, gateway based on loanRequest.approved value
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    
  }

  @Deployment(resources = {"org/camunda/bpm/engine/spring/test/jpa/JPASpringTest.bpmn20.xml"})
  public void testJpaVariableDisapprovalPath() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("customerName", "Jane Doe");
    variables.put("amount", 50000);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("LoanRequestProcess", variables);
    
    // Variable should be present containing the loanRequest created by the spring bean
    Object value = runtimeService.getVariable(processInstance.getId(), "loanRequest");
    assertNotNull(value);
    assertTrue(value instanceof LoanRequest);
    LoanRequest request = (LoanRequest) value;
    assertEquals("Jane Doe", request.getCustomerName());
    assertEquals(50000L, request.getAmount().longValue());
    assertFalse(request.isApproved());
    
    // We will disapprove the request, which will update the entity
    variables = new HashMap<String, Object>();
    variables.put("approvedByManager", Boolean.FALSE);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId(), variables);
    
    runtimeService.getVariable(processInstance.getId(), "loanRequest");
    request = (LoanRequest) value;
    assertFalse(request.isApproved());
    
    // If disapproved, an extra task will be available instead of the process ending
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Send rejection letter", task.getName());
  }
  
  
}
