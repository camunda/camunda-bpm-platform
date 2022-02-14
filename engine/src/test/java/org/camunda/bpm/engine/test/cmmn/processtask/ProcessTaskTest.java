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
package org.camunda.bpm.engine.test.cmmn.processtask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.cmmn.CmmnTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessTaskTest extends CmmnTest {

  protected final String PROCESS_TASK = "PI_ProcessTask_1";
  protected final String ONE_PROCESS_TASK_CASE = "oneProcessTaskCase";

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCallProcessAsConstant() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testCallProcessAsExpressionStartsWithDollar.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCallProcessAsExpressionStartsWithDollar() {
    // given
    // a deployed case definition
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE, Variables.createVariables().putValue("process", "oneTaskProcess")).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testCallProcessAsExpressionStartsWithHash.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCallProcessAsExpressionStartsWithHash() {
    // given
    // a deployed case definition
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE, Variables.createVariables().putValue("process", "oneTaskProcess")).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testCallLatestProcess.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCallLatestProcess() {
    // given
    String bpmnResourceName = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";

    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(bpmnResourceName)
        .deploy()
        .getId();

    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // latest process definition
    String latestProcessDefinitionId = repositoryService
      .createProcessDefinitionQuery()
      .latestVersion()
      .singleResult()
      .getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());
    // it is associated with the latest process definition
    assertEquals(latestProcessDefinitionId, processInstance.getProcessDefinitionId());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testCallProcessByDeployment.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCallProcessByDeployment() {
    // given

    String firstDeploymentId = repositoryService
      .createDeploymentQuery()
      .singleResult()
      .getId();

    String bpmnResourceName = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
    String deploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // latest process definition
    String processDefinitionIdInSameDeployment = repositoryService
      .createProcessDefinitionQuery()
      .deploymentId(firstDeploymentId)
      .singleResult()
      .getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());
    // it is associated with the correct process definition
    assertEquals(processDefinitionIdInSameDeployment, processInstance.getProcessDefinitionId());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testCallProcessByVersion.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCallProcessByVersion() {
    // given

    String bpmnResourceName = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
          .addClasspathResource(bpmnResourceName)
          .deploy()
          .getId();

    assertEquals(3, repositoryService.createProcessDefinitionQuery().count());

    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // latest process definition
    String processDefinitionIdInSecondDeployment = repositoryService
      .createProcessDefinitionQuery()
      .deploymentId(secondDeploymentId)
      .singleResult()
      .getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());
    // it is associated with the correct process definition
    assertEquals(processDefinitionIdInSecondDeployment, processInstance.getProcessDefinitionId());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testCallProcessByVersionAsExpressionStartsWithDollar.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCallProcessByVersionAsExpressionStartsWithDollar() {
    // given

    String bpmnResourceName = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";

    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
          .addClasspathResource(bpmnResourceName)
          .deploy()
          .getId();

    assertEquals(3, repositoryService.createProcessDefinitionQuery().count());

    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE,
        Variables.createVariables().putValue("myVersion", 2)).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // latest process definition
    String processDefinitionIdInSecondDeployment = repositoryService
      .createProcessDefinitionQuery()
      .deploymentId(secondDeploymentId)
      .singleResult()
      .getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());
    // it is associated with the correct process definition
    assertEquals(processDefinitionIdInSecondDeployment, processInstance.getProcessDefinitionId());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testCallProcessByVersionAsExpressionStartsWithHash.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCallProcessByVersionAsExpressionStartsWithHash() {
    // given

    String bpmnResourceName = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";

    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
          .addClasspathResource(bpmnResourceName)
          .deploy()
          .getId();

    assertEquals(3, repositoryService.createProcessDefinitionQuery().count());

    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE,
        Variables.createVariables().putValue("myVersion", 2)).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // latest process definition
    String processDefinitionIdInSecondDeployment = repositoryService
      .createProcessDefinitionQuery()
      .deploymentId(secondDeploymentId)
      .singleResult()
      .getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());
    // it is associated with the correct process definition
    assertEquals(processDefinitionIdInSecondDeployment, processInstance.getProcessDefinitionId());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputBusinessKey.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputBusinessKey() {
    // given
    String businessKey = "myBusinessKey";
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE, businessKey).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());
    // the business key has been set
    assertEquals(businessKey, processInstance.getBusinessKey());

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputDifferentBusinessKey.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputDifferentBusinessKey() {
    // given
    String businessKey = "myBusinessKey";
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE, businessKey).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // when
    caseService
      .withCaseExecution(processTaskId)
      .setVariable("myOwnBusinessKey", "myOwnBusinessKey")
      .manualStart();

    // then

    // there exists a process instance
    ExecutionEntity processInstance = (ExecutionEntity) queryProcessInstance();
    assertNotNull(processInstance);

    // the case instance id is set on called process instance
    assertEquals(caseInstanceId, processInstance.getCaseInstanceId());
    // the super case execution id is equals the processTaskId
    assertEquals(processTaskId, processInstance.getSuperCaseExecutionId());
    // the business key has been set
    assertEquals("myOwnBusinessKey", processInstance.getBusinessKey());
    assertFalse(businessKey.equals(processInstance.getBusinessKey()));

    TaskEntity task = (TaskEntity) queryTask();

    // the case instance id has been also set on the task
    assertEquals(caseInstanceId, task.getCaseInstanceId());
    // the case execution id should be null
    assertNull(task.getCaseExecutionId());

    // complete ////////////////////////////////////////////////////////

    taskService.complete(task.getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputSource.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputSource() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE,
        Variables.createVariables()
            .putValue("aVariable", "abc")
            .putValue("anotherVariable", 999)
            .putValue("aThirdVariable", "def"))
        .getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then

    // there exists a process instance
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals(999, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    taskService.complete(queryTask().getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputSourceDifferentTarget.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputSourceDifferentTarget() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // when
    caseService
      .withCaseExecution(processTaskId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .setVariable("aThirdVariable", "def")
      .manualStart();

    // then

    // there exists a process instance
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("myVariable".equals(name)) {
        assertEquals("myVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("myAnotherVariable".equals(name)) {
        assertEquals("myAnotherVariable", name);
        assertEquals(999, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    taskService.complete(queryTask().getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputSource.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputSourceNullValue() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then

    // there exists a process instance
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();

      if (!"aVariable".equals(name) && !"anotherVariable".equals(name)) {
        fail("Found an unexpected variable: '"+name+"'");
      }

      assertNull(variable.getValue());
    }

    // complete ////////////////////////////////////////////////////////

    taskService.complete(queryTask().getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputSourceExpression.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputSourceExpression() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE,
        Variables.createVariables().putValue("aVariable", "abc")
    .putValue("anotherVariable", 999)).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then

    // there exists a process instance
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals((long) 1000, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    taskService.complete(queryTask().getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputAll.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputAll() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE,
        Variables.createVariables().putValue("aVariable", "abc").putValue("anotherVariable", 999)).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then

    // there exists a process instance
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals(999, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    taskService.complete(queryTask().getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputAllLocal.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputAllLocal() {
    // given
    createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("aVariable", "abc")
      .setVariableLocal("aLocalVariable", "def")
      .manualStart();

    // then only the local variable is mapped to the sub process instance
    ProcessInstance subProcessInstance = queryProcessInstance();

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(subProcessInstance.getId())
        .list();

    assertEquals(1, variables.size());
    assertEquals("aLocalVariable", variables.get(0).getName());
  }


  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputOverlapping.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputOverlapping() {
    // specifics should override "all"
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // when
    caseService
      .withCaseExecution(processTaskId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .manualStart();

    // then

    // there exists a process instance
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstance.getId())
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals((long) 1000, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    taskService.complete(queryTask().getId());
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCaseWithManualActivation.cmmn"
    })
  @Test
  public void testProcessNotFound() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    try {
      // when
      caseService
        .withCaseExecution(processTaskId)
        .manualStart();
      fail("It should not be possible to start a process instance.");
    } catch (ProcessEngineException e) {}

    // complete //////////////////////////////////////////////////////////

    terminate(caseInstanceId);
    close(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCompleteSimpleProcess() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    Task task = queryTask();

    // when
    taskService.complete(task.getId());

    // then

    // the process instance has been completed
    ProcessInstance processInstance = queryProcessInstance();
    assertNull(processInstance);

    // the case execution associated with the
    // process task has been completed
    CaseExecution processTask = queryCaseExecutionByActivityId(PROCESS_TASK);
    assertNull(processTask);

    // complete ////////////////////////////////////////////////////////

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputSource.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testOutputSource() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    String processInstanceId = queryProcessInstance().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "abc");
    runtimeService.setVariable(processInstanceId, "anotherVariable", 999);
    runtimeService.setVariable(processInstanceId, "aThirdVariable", "def");

    String taskId = queryTask().getId();

    // when
    // should also complete process instance
    taskService.complete(taskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals(999, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputSourceDifferentTarget.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testOutputSourceDifferentTarget() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    String processInstanceId = queryProcessInstance().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "abc");
    runtimeService.setVariable(processInstanceId, "anotherVariable", 999);

    String taskId = queryTask().getId();

    // when
    // should also complete process instance
    taskService.complete(taskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("myVariable".equals(name)) {
        assertEquals("myVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("myAnotherVariable".equals(name)) {
        assertEquals("myAnotherVariable", name);
        assertEquals(999, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputSource.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testOutputSourceNullValue() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    String processInstanceId = queryProcessInstance().getId();

    String taskId = queryTask().getId();

    // when
    // should also complete process instance
    taskService.complete(taskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if (!"aVariable".equals(name) && !"anotherVariable".equals(name)) {
        fail("Found an unexpected variable: '" + name + "'");
      }

      assertNull(variable.getValue());
    }

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputSourceExpression.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testOutputSourceExpression() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    String processInstanceId = queryProcessInstance().getId();

    String taskId = queryTask().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "abc");
    runtimeService.setVariable(processInstanceId, "anotherVariable", 999);

    // when
    // should also complete process instance
    taskService.complete(taskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals((long) 1000, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputAll.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testOutputAll() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    String processInstanceId = queryProcessInstance().getId();

    String taskId = queryTask().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "abc");
    runtimeService.setVariable(processInstanceId, "anotherVariable", 999);

    // when
    // should also complete process instance
    taskService.complete(taskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals(999, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputOverlapping.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testOutputOverlapping() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    String processInstanceId = queryProcessInstance().getId();

    String taskId = queryTask().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "abc");
    runtimeService.setVariable(processInstanceId, "anotherVariable", 999);

    // when
    // should also complete process instance
    taskService.complete(taskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals((long) 1000, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputAllWithManualActivation.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testOutputVariablesShouldNotExistAnymore() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    caseService
      .withCaseExecution(processTaskId)
      // set variables local
      .setVariableLocal("aVariable", "xyz")
      .setVariableLocal("anotherVariable", 123)
      .manualStart();

    String processInstanceId = queryProcessInstance().getId();

    String taskId = queryTask().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "abc");
    runtimeService.setVariable(processInstanceId, "anotherVariable", 999);

    // when
    // should also complete process instance
    taskService.complete(taskId);

    // then

    // the variables has been deleted
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertTrue(variables.isEmpty());

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testVariablesRoundtrip.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testVariablesRoundtrip() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    caseService
      .withCaseExecution(processTaskId)
      .setVariable("aVariable", "xyz")
      .setVariable("anotherVariable", 123);

    String processInstanceId = queryProcessInstance().getId();

    String taskId = queryTask().getId();

    runtimeService.setVariable(processInstanceId, "aVariable", "abc");
    runtimeService.setVariable(processInstanceId, "anotherVariable", 999);

    // when
    // should also complete process instance
    taskService.complete(taskId);

    // then

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId)
        .list();

    assertEquals(2, variables.size());
    assertFalse(variables.get(0).getName().equals(variables.get(1).getName()));

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals(999, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testInputOutputAll.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputOutputAllTypedVariables() {
    String variableName = "aVariable";
    String variableName2 = "anotherVariable";
    String variableName3 = "theThirdVariable";
    TypedValue variableValue = Variables.stringValue("abc");
    TypedValue variableValue2 = Variables.longValue(null);

    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE,
        Variables.createVariables()
        .putValue(variableName, variableValue)
        .putValue(variableName2, variableValue2))
        .getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    String processInstanceId = queryProcessInstance().getId();

    TypedValue value = runtimeService.getVariableTyped(processInstanceId, variableName);
    assertThat(value).isEqualTo(variableValue);
    value = runtimeService.getVariableTyped(processInstanceId, variableName2);
    assertThat(value).isEqualTo(variableValue2);

    String taskId = queryTask().getId();

    TypedValue variableValue3 = Variables.integerValue(1);
    runtimeService.setVariable(processInstanceId, variableName3, variableValue3);

    // should also complete process instance
    taskService.complete(taskId);

    value = caseService.getVariableTyped(caseInstanceId, variableName3);

    assertThat(value).isEqualTo(variableValue3);

    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testVariablesRoundtrip.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testInputOutputLimitedTypedVariables() {
    String variableName = "aVariable";
    String variableName2 = "anotherVariable";
    TypedValue caseVariableValue = Variables.stringValue("abc");
    TypedValue caseVariableValue2 = Variables.integerValue(null);

    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE,
        Variables.createVariables().putValue(variableName, caseVariableValue)
        .putValue(variableName2, caseVariableValue2)).getId();

    String processInstanceId = queryProcessInstance().getId();
    TypedValue value = runtimeService.getVariableTyped(processInstanceId, variableName);
    assertThat(value).isEqualTo(caseVariableValue);
    value = runtimeService.getVariableTyped(processInstanceId, variableName2);
    assertThat(value).isEqualTo(caseVariableValue2);


    TypedValue processVariableValue = Variables.stringValue("cba");
    TypedValue processVariableValue2 = Variables.booleanValue(null);
    runtimeService.setVariable(processInstanceId, variableName, processVariableValue);
    runtimeService.setVariable(processInstanceId, variableName2, processVariableValue2);

    // should also complete process instance
    taskService.complete(queryTask().getId());

    value = caseService.getVariableTyped(caseInstanceId, variableName);
    assertThat(value).isEqualTo(processVariableValue);
    value = caseService.getVariableTyped(caseInstanceId, variableName2);
    assertThat(value).isEqualTo(processVariableValue2);
    // complete ////////////////////////////////////////////////////////

    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCompleteProcessTask() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    try {
      // when
      caseService
        .withCaseExecution(processTaskId)
        .complete();
      fail("It should not be possible to complete a process task, while the process instance is running.");
    } catch (NotAllowedException e) {}

    // complete ////////////////////////////////////////////////////////

    String processInstanceId = queryProcessInstance().getId();

    String taskId = queryTask().getId();

    taskService.complete(taskId);
    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testCompleteProcessTaskAfterTerminateSubProcessInstance() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    String processInstanceId = queryProcessInstance().getId();

    runtimeService.deleteProcessInstance(processInstanceId, null);

    ProcessInstance processInstance = queryProcessInstance();
    assertNull(processInstance);

    try {
      // when
      caseService
        .withCaseExecution(processTaskId)
        .complete();
      fail("It should not be possible to complete a process task");
    } catch (Exception e) {}

    // complete ////////////////////////////////////////////////////////

    terminate(caseInstanceId);
    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testTerminateProcessTask() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // when
    // terminate process task
    terminate(processTaskId);

    // then

    // the process instance is still running
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    // complete ////////////////////////////////////////////////////////

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    testRule.assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testTerminateSubProcessInstance() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    String processInstanceId = queryProcessInstance().getId();

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    ProcessInstance processInstance = queryProcessInstance();
    assertNull(processInstance);

    // the case execution associated with the process task
    // does still exist and is active.
    CaseExecution processTask = queryCaseExecutionByActivityId(PROCESS_TASK);

    assertNotNull(processTask);

    assertTrue(processTask.isActive());

    // complete ////////////////////////////////////////////////////////

    terminate(caseInstanceId);
    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testSuspendProcessTask() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // when
    // suspend process task
    suspend(processTaskId);

    // then
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);
    assertFalse(processInstance.isSuspended());

    Task task = queryTask();
    assertNotNull(task);
    assertFalse(task.isSuspended());

    // complete ////////////////////////////////////////////////////////

    resume(processTaskId);
    terminate(caseInstanceId);
    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

    taskService.complete(task.getId());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testSuspendSubProcessInstance() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    String processInstanceId = queryProcessInstance().getId();

    // when
    // suspend sub process instance
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance processInstance = queryProcessInstance();
    assertTrue(processInstance.isSuspended());

    // the case execution associated with the process task
    // is still active
    CaseExecution processTask = queryCaseExecutionByActivityId(PROCESS_TASK);
    assertTrue(processTask.isActive());

    // complete ////////////////////////////////////////////////////////

    runtimeService.activateProcessInstanceById(processInstanceId);

    String taskId = queryTask().getId();
    taskService.complete(taskId);
    testRule.assertProcessEnded(processInstanceId);

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testResumeProcessTask() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    suspend(processTaskId);

    CaseExecution processTask = queryCaseExecutionByActivityId(PROCESS_TASK);
    assertFalse(processTask.isActive());

    ProcessInstance processInstance = queryProcessInstance();
    assertFalse(processInstance.isSuspended());

    // when
    resume(processTaskId);

    // then
    processInstance = queryProcessInstance();
    assertFalse(processInstance.isSuspended());

    processTask = queryCaseExecutionByActivityId(PROCESS_TASK);
    assertTrue(processTask.isActive());

    // complete ////////////////////////////////////////////////////////

    String taskId = queryTask().getId();
    taskService.complete(taskId);
    testRule.assertProcessEnded(processInstance.getId());

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testNonBlockingProcessTask.cmmn",
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"
    })
  @Test
  public void testNonBlockingProcessTask() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    // then
    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    Task task = queryTask();
    assertNotNull(task);

    CaseExecution processTask = queryCaseExecutionByActivityId(PROCESS_TASK);
    assertNull(processTask);

    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());

    // complete ////////////////////////////////////////////////////////

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);

    String taskId = queryTask().getId();
    taskService.complete(taskId);
    testRule.assertProcessEnded(processInstance.getId());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testProcessInstanceCompletesInOneGo.cmmn",
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testProcessInstanceCompletesInOneGo.bpmn20.xml"
    })
  @Test
  public void testProcessInstanceCompletesInOneGo() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    // then
    ProcessInstance processInstance = queryProcessInstance();
    assertNull(processInstance);

    CaseExecution processTask = queryCaseExecutionByActivityId(PROCESS_TASK);
    assertNull(processTask);

    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());

    // complete ////////////////////////////////////////////////////////

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testNonBlockingProcessTaskAndProcessInstanceCompletesInOneGo.cmmn",
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testProcessInstanceCompletesInOneGo.bpmn20.xml"
    })
  @Test
  public void testNonBlockingProcessTaskAndProcessInstanceCompletesInOneGo() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    // then
    ProcessInstance processInstance = queryProcessInstance();
    assertNull(processInstance);

    CaseExecution processTask = queryCaseExecutionByActivityId(PROCESS_TASK);
    assertNull(processTask);

    CaseInstance caseInstance = caseService
        .createCaseInstanceQuery()
        .singleResult();
    assertNotNull(caseInstance);
    assertTrue(caseInstance.isCompleted());

    // complete ////////////////////////////////////////////////////////

    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testStartProcessInstanceAsync.cmmn",
      "org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testStartProcessInstanceAsync.bpmn20.xml"
    })
  @Test
  public void testStartProcessInstanceAsync() {
    // given
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    String processTaskId = queryCaseExecutionByActivityId(PROCESS_TASK).getId();

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);

    ProcessInstance processInstance = queryProcessInstance();
    assertNotNull(processInstance);

    // complete ////////////////////////////////////////////////////////

    managementService.executeJob(job.getId());
    close(caseInstanceId);
    testRule.assertCaseEnded(caseInstanceId);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneProcessTaskCaseWithManualActivation.cmmn"})
  @Test
  public void testActivityType() {
    // given
    createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();

    // when
    CaseExecution processTask = queryCaseExecutionByActivityId(PROCESS_TASK);

    // then
    assertEquals("processTask", processTask.getActivityType());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputAll.cmmn",
      "org/camunda/bpm/engine/test/cmmn/processtask/subProcessWithError.bpmn"})
  @Test
  public void testOutputWhenErrorOccurs() {
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    Task task = queryTask();
    assertThat(task.getName()).isEqualTo("SubTask");
    String variableName = "foo";
    Object variableValue = "bar";
    runtimeService.setVariable(task.getProcessInstanceId(), variableName, variableValue);
    taskService.complete(task.getId());

    Object variable = caseService.getVariable(caseInstanceId, variableName);
    assertThat(variable).isNotNull();
    assertThat(variable).isEqualTo(variableValue);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/processtask/ProcessTaskTest.testOutputAll.cmmn",
  "org/camunda/bpm/engine/test/cmmn/processtask/subProcessWithThrownError.bpmn"})
  @Test
  public void testOutputWhenThrownBpmnErrorOccurs() {
    String caseInstanceId = createCaseInstanceByKey(ONE_PROCESS_TASK_CASE).getId();
    Task task = queryTask();
    assertThat(task.getName()).isEqualTo("SubTask");
    String variableName = "foo";
    Object variableValue = "bar";
    runtimeService.setVariable(task.getProcessInstanceId(), variableName, variableValue);
    taskService.complete(task.getId());

    Object variable = caseService.getVariable(caseInstanceId, variableName);
    assertThat(variable).isNotNull();
    assertThat(variable).isEqualTo(variableValue);
  }

  protected ProcessInstance queryProcessInstance() {
    return runtimeService
        .createProcessInstanceQuery()
        .singleResult();
  }

  protected Task queryTask() {
    return taskService
        .createTaskQuery()
        .singleResult();
  }


}
