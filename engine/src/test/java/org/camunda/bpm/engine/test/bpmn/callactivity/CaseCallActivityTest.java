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
package org.camunda.bpm.engine.test.bpmn.callactivity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.cmmn.CaseDefinitionNotFoundException;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Roman Smirnov
 *
 */
public class CaseCallActivityTest extends CmmnProcessEngineTestCase {

  protected final String PROCESS_DEFINITION_KEY= "process";
  protected final String ONE_TASK_CASE = "oneTaskCase";
  protected final String CALL_ACTIVITY_ID = "callActivity";
  protected final String USER_TASK_ID = "userTask";
  protected final String HUMAN_TASK_ID = "PI_HumanTask_1";

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseAsConstant.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseAsConstant() {
    // given
    // a deployed process definition and case definition

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());

    // complete
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseAsExpressionStartsWithDollar.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseAsExpressionStartsWithDollar() {
    // given
    // a deployed process definition and case definition

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Variables.createVariables().putValue(ONE_TASK_CASE, ONE_TASK_CASE)).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());

    // complete
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseAsExpressionStartsWithHash.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseAsExpressionStartsWithHash() {
    // given
    // a deployed process definition and case definition

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Variables.createVariables().putValue(ONE_TASK_CASE, ONE_TASK_CASE)).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());

    // complete
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseWithCompositeExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  public void testCallCaseWithCompositeExpression() {
    // given
    // a deployed process definition and case definition

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());

    // complete
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallLatestCase.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallLatestCase() {
    // given
    String cmmnResourceName = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(cmmnResourceName)
        .deploy()
        .getId();

    assertEquals(2, repositoryService.createCaseDefinitionQuery().count());

    String latestCaseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(ONE_TASK_CASE)
        .latestVersion()
        .singleResult()
        .getId();

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());
    assertEquals(latestCaseDefinitionId, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseByDeployment.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseByDeployment() {
    // given

    String firstDeploymentId = repositoryService
      .createDeploymentQuery()
      .singleResult()
      .getId();

    String cmmnResourceName = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
    String deploymentId = repositoryService.createDeployment()
            .addClasspathResource(cmmnResourceName)
            .deploy()
            .getId();

    assertEquals(2, repositoryService.createCaseDefinitionQuery().count());

    String caseDefinitionIdInSameDeployment = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(ONE_TASK_CASE)
        .deploymentId(firstDeploymentId)
        .singleResult()
        .getId();

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());
    assertEquals(caseDefinitionIdInSameDeployment, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseByVersion.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseByVersion() {
    // given

    String cmmnResourceName = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(cmmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
          .addClasspathResource(cmmnResourceName)
          .deploy()
          .getId();

    assertEquals(3, repositoryService.createCaseDefinitionQuery().count());

    String caseDefinitionIdInSecondDeployment = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey(ONE_TASK_CASE)
      .deploymentId(secondDeploymentId)
      .singleResult()
      .getId();

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());
    assertEquals(caseDefinitionIdInSecondDeployment, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseByVersionAsExpression.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseByVersionAsExpression() {
    // given

    String cmmnResourceName = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(cmmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
          .addClasspathResource(cmmnResourceName)
          .deploy()
          .getId();

    assertEquals(3, repositoryService.createCaseDefinitionQuery().count());

    String caseDefinitionIdInSecondDeployment = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(ONE_TASK_CASE)
        .deploymentId(secondDeploymentId)
        .singleResult()
        .getId();

    VariableMap variables = Variables.createVariables().putValue("myVersion", 2);

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables).getId();

    // then
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(caseDefinitionIdInSecondDeployment, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseAsConstant.bpmn20.xml" })
  public void testCaseNotFound() {
    // given

    try {
      // when
      startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
      fail("It should not be possible to start a not existing case instance.");
    } catch (CaseDefinitionNotFoundException e) {
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testInputBusinessKey.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputBusinessKey() {
    // given
    String businessKey = "myBusinessKey";

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, null, businessKey).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());
    assertEquals(businessKey, subCaseInstance.getBusinessKey());

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testInputDifferentBusinessKey.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputDifferentBusinessKey() {
    // given
    String myBusinessKey = "myBusinessKey";
    String myOwnBusinessKey = "myOwnBusinessKey";

    VariableMap variables = Variables.createVariables().putValue(myOwnBusinessKey, myOwnBusinessKey);

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables, myBusinessKey).getId();

    // then
    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    assertEquals(callActivityId, subCaseInstance.getSuperExecutionId());
    assertEquals(myOwnBusinessKey, subCaseInstance.getBusinessKey());

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testInputSource.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputSource() {
    // given

    VariableMap parameters = Variables.createVariables()
      .putValue("aVariable", "abc")
      .putValue("anotherVariable", 999)
      .putValue("aThirdVariable", "def");

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, parameters).getId();

    // then
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(subCaseInstance.getId())
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

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
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testInputSourceDifferentTarget.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputSourceDifferentTarget() {
    // given

    VariableMap parameters = Variables.createVariables()
      .putValue("aVariable", "abc")
      .putValue("anotherVariable", 999)
      .putValue("aThirdVariable", "def");

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, parameters).getId();

    // then
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(subCaseInstance.getId())
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

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
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testInputSource.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputSourceNullValue() {
    // given

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    // then
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(subCaseInstance.getId())
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

    for (VariableInstance variable : variables) {
      String name = variable.getName();

      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }

      assertNull(variable.getValue());
    }

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testInputSourceExpression.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputSourceExpression() {
    // given
    VariableMap parameters = Variables.createVariables()
        .putValue("aVariable", "abc")
        .putValue("anotherVariable", 999);

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, parameters).getId();

    // then
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(subCaseInstance.getId())
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());

      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals((long)1000, variable.getValue());

      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testInputSourceAsCompositeExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  public void testInputSourceAsCompositeExpression() {
    // given
    VariableMap parameters = Variables.createVariables()
      .putValue("aVariable", "abc")
      .putValue("anotherVariable", 999);

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, parameters).getId();

    // then
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    List<VariableInstance> variables = runtimeService
      .createVariableInstanceQuery()
      .caseInstanceIdIn(subCaseInstance.getId())
      .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("Prefixabc", variable.getValue());

      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals("Prefix" + (long)1000, variable.getValue());

      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testInputAll.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputAll() {
    // given
    VariableMap parameters = Variables.createVariables()
        .putValue("aVariable", "abc")
        .putValue("anotherVariable", 999);

    // when
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, parameters).getId();

    // then
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(subCaseInstance.getId())
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

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
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCompleteCase.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCompleteCase() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then
    Task userTask = queryTaskByActivityId(USER_TASK_ID);
    assertNotNull(userTask);

    Execution callActivity = queryExecutionByActivityId(CALL_ACTIVITY_ID);
    assertNull(callActivity);

    // complete ////////////////////////////////////////////////////////

    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    taskService.complete(userTask.getId());
    assertCaseEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testOutputSource.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputSource() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .setVariable("aThirdVariable", "def")
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(superProcessInstanceId)
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

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
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    String taskId = queryTaskByActivityId(USER_TASK_ID).getId();
    taskService.complete(taskId);
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testOutputSourceDifferentTarget.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputSourceDifferentTarget() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(superProcessInstanceId)
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

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
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    String taskId = queryTaskByActivityId(USER_TASK_ID).getId();
    taskService.complete(taskId);
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testOutputSource.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputSourceNullValue() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(superProcessInstanceId)
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }

      assertNull(variable.getValue());
    }

    // complete ////////////////////////////////////////////////////////
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    String taskId = queryTaskByActivityId(USER_TASK_ID).getId();
    taskService.complete(taskId);
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testOutputSourceExpression.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputSourceExpression() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(superProcessInstanceId)
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

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
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    String taskId = queryTaskByActivityId(USER_TASK_ID).getId();
    taskService.complete(taskId);
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testOutputSourceAsCompositeExpression.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
  })
  public void testOutputSourceAsCompositeExpression() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then
    List<VariableInstance> variables = runtimeService
      .createVariableInstanceQuery()
      .processInstanceIdIn(superProcessInstanceId)
      .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("Prefixabc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals("Prefix"+(long) 1000, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    String taskId = queryTaskByActivityId(USER_TASK_ID).getId();
    taskService.complete(taskId);
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testOutputAll.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputAll() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(superProcessInstanceId)
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

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
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    String taskId = queryTaskByActivityId(USER_TASK_ID).getId();
    taskService.complete(taskId);
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testOutputAll.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputVariablesShouldNotExistAnymore() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    String callActivityId = queryExecutionByActivityId(CALL_ACTIVITY_ID).getId();

    VariableMap parameters = Variables.createVariables()
      .putValue("aVariable", "xyz")
      .putValue("anotherVariable", 123);

    runtimeService.setVariablesLocal(callActivityId, parameters);

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then

    // the variables has been deleted
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(superProcessInstanceId)
        .list();

    assertTrue(variables.isEmpty());

    // complete ////////////////////////////////////////////////////////
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    String taskId = queryTaskByActivityId(USER_TASK_ID).getId();
    taskService.complete(taskId);
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testVariablesRoundtrip.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testVariablesRoundtrip() {
    // given
    VariableMap parameters = Variables.createVariables()
      .putValue("aVariable", "xyz")
      .putValue("anotherVariable", 999);

    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY, parameters).getId();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 1000)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when
    complete(humanTaskId);

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(superProcessInstanceId)
        .list();

    assertFalse(variables.isEmpty());
    assertEquals(2, variables.size());

    for (VariableInstance variable : variables) {
      String name = variable.getName();
      if ("aVariable".equals(name)) {
        assertEquals("aVariable", name);
        assertEquals("abc", variable.getValue());
      } else if ("anotherVariable".equals(name)) {
        assertEquals("anotherVariable", name);
        assertEquals(1000, variable.getValue());
      } else {
        fail("Found an unexpected variable: '"+name+"'");
      }
    }

    // complete ////////////////////////////////////////////////////////
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    String taskId = queryTaskByActivityId(USER_TASK_ID).getId();
    taskService.complete(taskId);
    assertProcessEnded(superProcessInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testOutputAll.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn"
    })
  public void testCallCaseOutputAllVariablesTypedToProcess(){
    startProcessInstanceByKey("process");
    CaseInstance caseInstance = queryOneTaskCaseInstance();
    String variableName = "foo";
    String variableName2 = "null";
    TypedValue variableValue = Variables.stringValue("bar");
    TypedValue variableValue2 = Variables.integerValue(null);
    caseService.withCaseExecution(caseInstance.getId())
      .setVariable(variableName, variableValue)
      .setVariable(variableName2, variableValue2)
      .execute();
    complete(caseInstance.getId());

    Task task = taskService.createTaskQuery().singleResult();
    TypedValue value = runtimeService.getVariableTyped(task.getProcessInstanceId(), variableName);
    assertThat(value, is(variableValue));
    value = runtimeService.getVariableTyped(task.getProcessInstanceId(), variableName2);
    assertThat(value, is(variableValue2));
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseAsConstant.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testDeleteProcessInstance() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    // when
    runtimeService.deleteProcessInstance(superProcessInstanceId, null);

    // then
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    CaseInstance subCaseInstance = queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    // complete ////////////////////////////////////////////////////////
    terminate(subCaseInstanceId);
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseAsConstant.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testSuspendProcessInstance() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();

    // when (1)
    runtimeService.suspendProcessInstanceById(superProcessInstanceId);

    // then
    Execution superProcessInstance = queryExecutionById(superProcessInstanceId);
    assertNotNull(superProcessInstance);
    assertTrue(superProcessInstance.isSuspended());

    CaseInstance subCaseInstance = queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isActive());

    try {
      // when
      complete(humanTaskId);
      fail("The super process instance is suspended.");
    } catch (Exception e) {
      // expected
    }

    // complete ////////////////////////////////////////////////////////
    runtimeService.activateProcessInstanceById(superProcessInstanceId);

    complete(humanTaskId);
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstanceId);
    assertProcessEnded(superProcessInstanceId);

    repositoryService.deleteDeployment(deploymentId, true);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseAsConstant.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testTerminateSubCaseInstance() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    // when
    terminate(subCaseInstanceId);

    // then
    CmmnExecution subCaseInstance = (CmmnExecution) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isTerminated());

    Execution callActivity = queryExecutionByActivityId(CALL_ACTIVITY_ID);
    assertNotNull(callActivity);

    // complete ////////////////////////////////////////////////////////

    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    runtimeService.deleteProcessInstance(superProcessInstanceId, null);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCallCaseAsConstant.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn"
    })
  public void testSuspendSubCaseInstance() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();
    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    // when
    suspend(subCaseInstanceId);

    // then
    CmmnExecution subCaseInstance = (CmmnExecution) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isSuspended());

    Execution callActivity = queryExecutionByActivityId(CALL_ACTIVITY_ID);
    assertNotNull(callActivity);

    // complete ////////////////////////////////////////////////////////

    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    runtimeService.deleteProcessInstance(superProcessInstanceId, null);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivityTest.testCompletionOfCaseWithTwoTasks.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn"
    })
  public void testCompletionOfTwoHumanTasks() {
    // given
    String superProcessInstanceId = startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getId();

    // when (1)
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();
    manualStart(humanTaskId);
    complete(humanTaskId);

    // then (1)

    assertEquals(0, taskService.createTaskQuery().count());

    // when (2)
    humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_2").getId();
    manualStart(humanTaskId);
    complete(humanTaskId);

    // then (2)
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    assertEquals(superProcessInstanceId, task.getProcessInstanceId());
    assertEquals("userTask", task.getTaskDefinitionKey());
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivity.testSubProcessLocalInputAllVariables.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testSubProcessLocalInputAllVariables() {
    ProcessInstance processInstance = startProcessInstanceByKey("subProcessLocalInputAllVariables");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when setting a variable in a process instance
    runtimeService.setVariable(processInstance.getId(), "callingProcessVar1", "val1");

    // and executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then only the local variable specified in the io mapping is passed to the called instance
    CaseExecutionEntity calledInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(calledInstance);

    Map<String, Object> calledInstanceVariables = caseService.getVariables(calledInstance.getId());
    assertEquals(1, calledInstanceVariables.size());
    assertEquals("val2", calledInstanceVariables.get("inputParameter"));

    // when setting a variable in the called instance
    caseService.setVariable(calledInstance.getId(), "calledCaseVar1", 42L);

    // and completing it
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();
    complete(humanTaskId);

    // then the call activity output variable has been mapped to the process instance execution
    // and the output mapping variable as well
    Map<String, Object> callingInstanceVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(3, callingInstanceVariables.size());
    assertEquals("val1", callingInstanceVariables.get("callingProcessVar1"));
    assertEquals(42L, callingInstanceVariables.get("calledCaseVar1"));
    assertEquals(43L, callingInstanceVariables.get("outputParameter"));
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivity.testSubProcessLocalInputSingleVariable.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testSubProcessLocalInputSingleVariable() {
    ProcessInstance processInstance = startProcessInstanceByKey("subProcessLocalInputSingleVariable");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when setting a variable in a process instance
    runtimeService.setVariable(processInstance.getId(), "callingProcessVar1", "val1");

    // and executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then the local variable specified in the io mapping is passed to the called instance
    CaseExecutionEntity calledInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(calledInstance);

    Map<String, Object> calledInstanceVariables = caseService.getVariables(calledInstance.getId());
    assertEquals(1, calledInstanceVariables.size());
    assertEquals("val2", calledInstanceVariables.get("mappedInputParameter"));

    // when setting a variable in the called instance
    caseService.setVariable(calledInstance.getId(), "calledCaseVar1", 42L);

    // and completing it
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();
    complete(humanTaskId);

    // then the call activity output variable has been mapped to the process instance execution
    // and the output mapping variable as well
    Map<String, Object> callingInstanceVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(4, callingInstanceVariables.size());
    assertEquals("val1", callingInstanceVariables.get("callingProcessVar1"));
    assertEquals("val2", callingInstanceVariables.get("mappedInputParameter"));
    assertEquals(42L, callingInstanceVariables.get("calledCaseVar1"));
    assertEquals(43L, callingInstanceVariables.get("outputParameter"));
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivity.testSubProcessLocalInputSingleVariableExpression.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testSubProcessLocalInputSingleVariableExpression() {
    ProcessInstance processInstance = startProcessInstanceByKey("subProcessLocalInputSingleVariableExpression");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then the local input parameter can be resolved because its source expression variable
    // is defined in the call activity's input mapping
    CaseExecutionEntity calledInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(calledInstance);

    Map<String, Object> calledInstanceVariables = caseService.getVariables(calledInstance.getId());
    assertEquals(1, calledInstanceVariables.size());
    assertEquals(43L, calledInstanceVariables.get("mappedInputParameter"));

    // and completing it
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();
    complete(humanTaskId);

    // and executing a call activity in parameter where the source variable is not mapped by an activity
    // input parameter fails

    Task beforeSecondCallActivityTask = taskService.createTaskQuery().singleResult();
    runtimeService.setVariable(processInstance.getId(), "globalVariable", "42");

    try {
      taskService.complete(beforeSecondCallActivityTask.getId());
      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertTextPresent("Cannot resolve identifier 'globalVariable'", e.getMessage());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivity.testSubProcessLocalOutputAllVariables.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/cmmn/oneTaskCaseWithManualActivation.cmmn" })
  public void testSubProcessLocalOutputAllVariables() {
    ProcessInstance processInstance = startProcessInstanceByKey("subProcessLocalOutputAllVariables");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when setting a variable in a process instance
    runtimeService.setVariable(processInstance.getId(), "callingProcessVar1", "val1");

    // and executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then all variables have been mapped into the called instance
    CaseExecutionEntity calledInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(calledInstance);

    Map<String, Object> calledInstanceVariables = caseService.getVariables(calledInstance.getId());
    assertEquals(2, calledInstanceVariables.size());
    assertEquals("val1", calledInstanceVariables.get("callingProcessVar1"));
    assertEquals("val2", calledInstanceVariables.get("inputParameter"));

    // when setting a variable in the called instance
    caseService.setVariable(calledInstance.getId(), "calledCaseVar1", 42L);

    // and completing it
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();
    manualStart(humanTaskId);
    complete(humanTaskId);

    // then only the output mapping variable has been mapped into the calling process instance
    Map<String, Object> callingInstanceVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(2, callingInstanceVariables.size());
    assertEquals("val1", callingInstanceVariables.get("callingProcessVar1"));
    assertEquals(43L, callingInstanceVariables.get("outputParameter"));
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/bpmn/callactivity/CaseCallActivity.testSubProcessLocalOutputSingleVariable.bpmn20.xml",
  "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn" })
  public void testSubProcessLocalOutputSingleVariable() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessLocalOutputSingleVariable");
    Task beforeCallActivityTask = taskService.createTaskQuery().singleResult();

    // when setting a variable in a process instance
    runtimeService.setVariable(processInstance.getId(), "callingProcessVar1", "val1");

    // and executing the call activity
    taskService.complete(beforeCallActivityTask.getId());

    // then all variables have been mapped into the called instance
    CaseExecutionEntity calledInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(calledInstance);

    Map<String, Object> calledInstanceVariables = caseService.getVariables(calledInstance.getId());
    assertEquals(2, calledInstanceVariables.size());
    assertEquals("val1", calledInstanceVariables.get("callingProcessVar1"));
    assertEquals("val2", calledInstanceVariables.get("inputParameter"));

    // when setting a variable in the called instance
    caseService.setVariable(calledInstance.getId(), "calledCaseVar1", 42L);

    // and completing it
    String humanTaskId = queryCaseExecutionByActivityId(HUMAN_TASK_ID).getId();
    complete(humanTaskId);

    // then only the output mapping variable has been mapped into the calling process instance
    Map<String, Object> callingInstanceVariables = runtimeService.getVariables(processInstance.getId());
    assertEquals(2, callingInstanceVariables.size());
    assertEquals("val1", callingInstanceVariables.get("callingProcessVar1"));
    assertEquals(43L, callingInstanceVariables.get("outputParameter"));
  }

  protected ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return startProcessInstanceByKey(processDefinitionKey, null);
  }

  protected ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
    return startProcessInstanceByKey(processDefinitionKey, variables, null);
  }

  protected ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables, String businessKey) {
    return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
  }

  protected CaseExecution queryCaseExecutionById(String id) {
    return caseService
        .createCaseExecutionQuery()
        .caseExecutionId(id)
        .singleResult();
  }

  protected CaseExecution queryCaseExecutionByActivityId(String activityId) {
    return caseService
        .createCaseExecutionQuery()
        .activityId(activityId)
        .singleResult();
  }

  protected CaseInstance queryOneTaskCaseInstance() {
    return caseService
        .createCaseInstanceQuery()
        .caseDefinitionKey(ONE_TASK_CASE)
        .singleResult();
  }

  protected Execution queryExecutionById(String id) {
    return runtimeService
        .createExecutionQuery()
        .executionId(id)
        .singleResult();
  }

  protected Execution queryExecutionByActivityId(String activityId) {
    return runtimeService
        .createExecutionQuery()
        .activityId(activityId)
        .singleResult();
  }

  protected Task queryTaskByActivityId(String activityId) {
    return taskService
        .createTaskQuery()
        .taskDefinitionKey(activityId)
        .singleResult();
  }

}
