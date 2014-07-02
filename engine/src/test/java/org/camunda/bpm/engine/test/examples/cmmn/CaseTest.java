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
package org.camunda.bpm.engine.test.examples.cmmn;

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CaseTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = "org/camunda/bpm/engine/test/examples/cmmn/loan-application.cmmn")
  public void testCreateCaseInstanceById() {
    // given
    // there exists a deployment containing a case definition with key "loanApplication"

    CaseDefinition caseDefinition = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey("loanApplication")
      .singleResult();

    assertNotNull(caseDefinition);

    // when
    // create a new case instance by id

    CaseInstance caseInstance = caseService
      .withCaseDefinition(caseDefinition.getId())
      .create();

    // then
    // the returned caseInstance is not null

    assertNotNull(caseInstance);

    // verify that the case instance is persisted using the API

    CaseInstance instance = caseService
      .createCaseInstanceQuery()
      .caseInstanceId(caseInstance.getId())
      .singleResult();

    assertNotNull(instance);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/examples/cmmn/loan-application.cmmn")
  public void testCreateCaseInstanceByKey() {
    // given
    // there exists a deployment containing a case definition with key "loanApplication"

    CaseDefinition caseDefinition = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey("loanApplication")
      .singleResult();

    assertNotNull(caseDefinition);

    // when
    // create a new case instance by key

    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey(caseDefinition.getKey())
      .create();

    // then
    // the returned caseInstance is not null

    assertNotNull(caseInstance);

    // verify that the case instance is persisted using the API

    CaseInstance instance = caseService
      .createCaseInstanceQuery()
      .caseInstanceId(caseInstance.getId())
      .singleResult();

    assertNotNull(instance);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/examples/cmmn/loan-application.cmmn")
  public void testCaseExecutionQuery() {
    // given
    // there exists a deployment containing a case definition with key "loanApplication"

    CaseDefinition caseDefinition = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey("loanApplication")
      .singleResult();

    assertNotNull(caseDefinition);

    // when
    // create a new case instance by key

    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey(caseDefinition.getKey())
      .create();

    // then
    // the returned caseInstance is not null

    assertNotNull(caseInstance);

    // verify that there are three case execution:
    // - the case instance itself (ie. for the casePlanModel)
    // - a case execution for the stage
    // - a case execution for the humanTask

    List<CaseExecution> caseExecutions = caseService
        .createCaseExecutionQuery()
        .caseInstanceId(caseInstance.getId())
        .list();

    assertEquals(3, caseExecutions.size());

    CaseExecution casePlanModelExecution = caseService
        .createCaseExecutionQuery()
        .activityId("CasePlanModel_1")
        .singleResult();

    assertNotNull(casePlanModelExecution);

    CaseExecution stageExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult();

    assertNotNull(stageExecution);

    CaseExecution humanTaskExecution = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_6")
        .singleResult();

    assertNotNull(humanTaskExecution);

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/examples/cmmn/loan-application.cmmn")
  public void testCaseInstanceQuery() {
    // given
    // there exists a deployment containing a case definition with key "loanApplication"

    CaseDefinition caseDefinition = repositoryService
      .createCaseDefinitionQuery()
      .caseDefinitionKey("loanApplication")
      .singleResult();

    assertNotNull(caseDefinition);

    // when
    // create a new case instance by key

    CaseInstance caseInstance = caseService
      .withCaseDefinitionByKey(caseDefinition.getKey())
      .create();

    // then
    // the returned caseInstance is not null

    assertNotNull(caseInstance);

    // verify that there is one caseInstance

    // only select ACTIVE case instances
    List<CaseInstance> caseInstances = caseService
        .createCaseInstanceQuery()
        .active()
        .list();

    assertEquals(1, caseInstances.size());
  }

}
