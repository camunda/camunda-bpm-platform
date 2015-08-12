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
package org.camunda.bpm.engine.test.cmmn.casetask;

import java.util.List;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CaseTaskTest extends CmmnProcessEngineTestCase {

  protected final String CASE_TASK = "PI_CaseTask_1";
  protected final String ONE_CASE_TASK_CASE = "oneCaseTaskCase";
  protected final String ONE_TASK_CASE = "oneTaskCase";

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseAsConstant() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testCallCaseAsExpressionStartsWithDollar.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseAsExpressionStartsWithDollar() {
    // given
    // a deployed case definition
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("oneTaskCase", ONE_TASK_CASE)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testCallCaseAsExpressionStartsWithHash.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseAsExpressionStartsWithHash() {
    // given
    // a deployed case definition
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("oneTaskCase", ONE_TASK_CASE)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testCallLatestCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallLatestCase() {
    // given
    String cmmnResourceName = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(cmmnResourceName)
        .deploy()
        .getId();

    assertEquals(3, repositoryService.createCaseDefinitionQuery().count());

    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    String latestCaseDefinitionId = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey(ONE_TASK_CASE)
      .latestVersion()
      .singleResult()
      .getId();

    // when:
    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());
    assertEquals(latestCaseDefinitionId, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testCallCaseByDeployment.cmmn",
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

    assertEquals(3, repositoryService.createCaseDefinitionQuery().count());

    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    String caseDefinitionIdInSameDeployment = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey(ONE_TASK_CASE)
      .deploymentId(firstDeploymentId)
      .singleResult()
      .getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());
    assertEquals(caseDefinitionIdInSameDeployment, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testCallCaseByVersion.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseByVersion() {
    // given

    String bpmnResourceName = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
          .addClasspathResource(bpmnResourceName)
          .deploy()
          .getId();

    assertEquals(4, repositoryService.createCaseDefinitionQuery().count());

    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    String caseDefinitionIdInSecondDeployment = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey(ONE_TASK_CASE)
      .deploymentId(secondDeploymentId)
      .singleResult()
      .getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());
    assertEquals(caseDefinitionIdInSecondDeployment, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testCallCaseByVersionAsExpressionStartsWithDollar.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseByVersionAsExpressionStartsWithDollar() {
    // given

    String bpmnResourceName = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
          .addClasspathResource(bpmnResourceName)
          .deploy()
          .getId();

    assertEquals(4, repositoryService.createCaseDefinitionQuery().count());

    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    String caseDefinitionIdInSecondDeployment = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey(ONE_TASK_CASE)
      .deploymentId(secondDeploymentId)
      .singleResult()
      .getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("myVersion", 2)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());
    assertEquals(caseDefinitionIdInSecondDeployment, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testCallCaseByVersionAsExpressionStartsWithHash.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCallCaseByVersionAsExpressionStartsWithHash() {
    // given

    String bpmnResourceName = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

    String secondDeploymentId = repositoryService.createDeployment()
            .addClasspathResource(bpmnResourceName)
            .deploy()
            .getId();

    String thirdDeploymentId = repositoryService.createDeployment()
          .addClasspathResource(bpmnResourceName)
          .deploy()
          .getId();

    assertEquals(4, repositoryService.createCaseDefinitionQuery().count());

    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    String caseDefinitionIdInSecondDeployment = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey(ONE_TASK_CASE)
      .deploymentId(secondDeploymentId)
      .singleResult()
      .getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("myVersion", 2)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());
    assertEquals(caseDefinitionIdInSecondDeployment, subCaseInstance.getCaseDefinitionId());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

    repositoryService.deleteDeployment(secondDeploymentId, true);
    repositoryService.deleteDeployment(thirdDeploymentId, true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testInputBusinessKey.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputBusinessKey() {
    // given
    String businessKey = "myBusinessKey";
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE, businessKey).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());
    assertEquals(businessKey, subCaseInstance.getBusinessKey());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testInputDifferentBusinessKey.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputDifferentBusinessKey() {
    // given
    String businessKey = "myBusinessKey";
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE, businessKey).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("myOwnBusinessKey", "myOwnBusinessKey")
      .manualStart();

    // then

    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    String superCaseExecutionId = subCaseInstance.getSuperCaseExecutionId();
    CaseExecution superCaseExecution = queryCaseExecutionById(superCaseExecutionId);

    assertEquals(caseTaskId, superCaseExecutionId);
    assertEquals(superCaseInstanceId, superCaseExecution.getCaseInstanceId());
    assertEquals("myOwnBusinessKey", subCaseInstance.getBusinessKey());

    // complete ////////////////////////////////////////////////////////

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testInputSource.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputSource() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .setVariable("aThirdVariable", "def")
      .manualStart();

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

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testInputSourceDifferentTarget.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputSourceDifferentTarget() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .manualStart();

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

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testInputSource.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputSourceNullValue() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

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

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testInputSourceExpression.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputSourceExpression() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .manualStart();

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

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testInputAll.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testInputAll() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .manualStart();

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

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertCaseEnded(subCaseInstance.getId());

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn"
    })
  public void testCaseNotFound() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .manualStart();
      fail("It should not be possible to start a not existing case instance.");
    } catch (NotFoundException e) {}

    // complete //////////////////////////////////////////////////////////

    caseService
      .withCaseExecution(caseTaskId)
      .disable();
    close(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCompleteSimpleCase() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();
    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then

    CaseExecution caseTask = queryCaseExecutionByActivityId("PI_CaseTask_1");
    assertNull(caseTask);

    // complete ////////////////////////////////////////////////////////

    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testOutputSource.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputSource() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .setVariable("aThirdVariable", "def")
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // when
    caseService
      .withCaseExecution(subCaseInstanceId)
      .close();

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(superCaseInstanceId)
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

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testOutputSourceDifferentTarget.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputSourceDifferentTarget() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // when
    caseService
      .withCaseExecution(subCaseInstanceId)
      .close();

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(superCaseInstanceId)
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

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testOutputSource.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputSourceNullValue() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();
    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // when
    caseService
      .withCaseExecution(subCaseInstanceId)
      .close();

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(superCaseInstanceId)
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

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testOutputSourceExpression.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputSourceExpression() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // when
    caseService
      .withCaseExecution(subCaseInstanceId)
      .close();

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(superCaseInstanceId)
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

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testOutputAll.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputAll() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // when
    caseService
      .withCaseExecution(subCaseInstanceId)
      .close();

    // then
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(superCaseInstanceId)
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

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testOutputAll.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testOutputVariablesShouldNotExistAnymore() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      // set variables local
      .setVariableLocal("aVariable", "xyz")
      .setVariableLocal("anotherVariable", 123)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // when
    caseService
      .withCaseExecution(subCaseInstanceId)
      .close();

    // then

    // the variables has been deleted
    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(superCaseInstanceId)
        .list();

    assertTrue(variables.isEmpty());

    // complete ////////////////////////////////////////////////////////

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testVariablesRoundtrip.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testVariablesRoundtrip() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      // set variables local
      .setVariable("aVariable", "xyz")
      .setVariable("anotherVariable", 123)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    caseService
      .withCaseExecution(subCaseInstanceId)
      .setVariable("aVariable", "abc")
      .setVariable("anotherVariable", 999)
      .execute();

    String humanTaskId = queryCaseExecutionByActivityId("PI_HumanTask_1").getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // when
    caseService
      .withCaseExecution(subCaseInstanceId)
      .close();

    // then

    List<VariableInstance> variables = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(superCaseInstanceId)
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

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testCompleteCaseTask() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .complete();
      fail("It should not be possible to complete a case task, while the case instance is active.");
    } catch (NotAllowedException e) {}


    // complete ////////////////////////////////////////////////////////

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();
    terminate(subCaseInstanceId);
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testTerminateCaseTask() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    CaseInstance subCaseInstance = queryOneTaskCaseInstance();
    assertTrue(subCaseInstance.isActive());

    // when
    terminate(caseTaskId);

    subCaseInstance = queryOneTaskCaseInstance();
    assertTrue(subCaseInstance.isActive());

    // complete ////////////////////////////////////////////////////////

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();
    terminate(subCaseInstanceId);
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testTerminateSubCaseInstance() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    // when
    terminate(subCaseInstanceId);

    // then
    CmmnExecution subCaseInstance = (CmmnExecution) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isTerminated());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK);
    assertNotNull(caseTask);
    assertTrue(caseTask.isActive());

    // complete ////////////////////////////////////////////////////////

    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testSuspendCaseTask() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    CaseInstance subCaseInstance = queryOneTaskCaseInstance();
    assertTrue(subCaseInstance.isActive());

    // when
    suspend(caseTaskId);

    subCaseInstance = queryOneTaskCaseInstance();
    assertTrue(subCaseInstance.isActive());

    // complete ////////////////////////////////////////////////////////

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();
    terminate(subCaseInstanceId);
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    terminate(superCaseInstanceId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testSuspendSubCaseInstance() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();

    // when
    suspend(subCaseInstanceId);

    // then
    CmmnExecution subCaseInstance = (CmmnExecution) queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);
    assertTrue(subCaseInstance.isSuspended());

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK);
    assertNotNull(caseTask);
    assertTrue(caseTask.isActive());

    // complete ////////////////////////////////////////////////////////

    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testResumeCaseTask() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    suspend(caseTaskId);

    CaseInstance subCaseInstance = queryOneTaskCaseInstance();
    assertTrue(subCaseInstance.isActive());

    // when
    resume(caseTaskId);

    // then
    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK);
    assertTrue(caseTask.isActive());

    subCaseInstance = queryOneTaskCaseInstance();
    assertTrue(subCaseInstance.isActive());

    // complete ////////////////////////////////////////////////////////

    String subCaseInstanceId = queryOneTaskCaseInstance().getId();
    terminate(subCaseInstanceId);
    close(subCaseInstanceId);
    assertCaseEnded(subCaseInstanceId);

    terminate(caseTaskId);
    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/casetask/CaseTaskTest.testNotBlockingCaseTask.cmmn",
      "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"
    })
  public void testNotBlockingCaseTask() {
    // given
    String superCaseInstanceId = createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();
    String caseTaskId = queryCaseExecutionByActivityId(CASE_TASK).getId();

    // when
    caseService
      .withCaseExecution(caseTaskId)
      .manualStart();

    // then
    CaseInstance subCaseInstance = queryOneTaskCaseInstance();
    assertNotNull(subCaseInstance);

    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK);
    assertNull(caseTask);

    CaseInstance superCaseInstance = caseService
        .createCaseInstanceQuery()
        .caseDefinitionKey(ONE_CASE_TASK_CASE)
        .singleResult();
    assertNotNull(superCaseInstance);
    assertTrue(superCaseInstance.isCompleted());

    // complete ////////////////////////////////////////////////////////

    close(superCaseInstanceId);
    assertCaseEnded(superCaseInstanceId);

    terminate(subCaseInstance.getId());
    close(subCaseInstance.getId());
    assertProcessEnded(subCaseInstance.getId());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/cmmn/oneCaseTaskCase.cmmn"})
  public void testActivityType() {
    // given
    createCaseInstanceByKey(ONE_CASE_TASK_CASE).getId();

    // when
    CaseExecution caseTask = queryCaseExecutionByActivityId(CASE_TASK);

    // then
    assertEquals("caseTask", caseTask.getActivityType());
  }

  protected CaseInstance createCaseInstanceByKey(String caseDefinitionKey) {
    return createCaseInstanceByKey(caseDefinitionKey, null, null);
  }

  protected CaseInstance createCaseInstanceByKey(String caseDefinitionKey, String businessKey) {
    return caseService
        .withCaseDefinitionByKey(caseDefinitionKey)
        .businessKey(businessKey)
        .create();
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

  protected Task queryTask() {
    return taskService
        .createTaskQuery()
        .singleResult();
  }

}
