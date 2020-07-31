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
package org.camunda.bpm.engine.test.api.cmmn;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CaseServiceMilestoneTest extends PluggableProcessEngineTest {

  protected final String DEFINITION_KEY = "oneMilestoneCase";
  protected final String MILESTONE_KEY = "PI_Milestone_1";

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneMilestoneCase.cmmn"
      })
  @Test
  public void testManualStart() {
    // given
    createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(MILESTONE_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .manualStart();
      fail();
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneMilestoneCase.cmmn"
      })
  @Test
  public void testDisable() {
    // given
    createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(MILESTONE_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .disable();
      fail();
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneMilestoneCase.cmmn"
      })
  @Test
  public void testReenable() {
    // given
    createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(MILESTONE_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .reenable();
      fail();
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneMilestoneCase.cmmn"
      })
  @Test
  public void testComplete() {
    // given
    createCaseInstance(DEFINITION_KEY).getId();
    String caseTaskId = queryCaseExecutionByActivityId(MILESTONE_KEY).getId();

    try {
      // when
      caseService
        .withCaseExecution(caseTaskId)
        .complete();
      fail();
    } catch (NotAllowedException e) {
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneMilestoneCase.cmmn"
      })
  @Test
  public void testTerminate() {
    // given
    createCaseInstance(DEFINITION_KEY).getId();
   
    String caseTaskId = queryCaseExecutionByActivityId(MILESTONE_KEY).getId();

    caseService
     .withCaseExecution(caseTaskId)
     .terminate();

    CaseExecution caseMilestone = queryCaseExecutionByActivityId(MILESTONE_KEY);
    assertNull(caseMilestone);
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/cmmn/oneMilestoneCase.cmmn"
      })
  @Test
  public void testTerminateNonFluent() {
    // given
    createCaseInstance(DEFINITION_KEY).getId();
    CaseExecution caseMilestone = queryCaseExecutionByActivityId(MILESTONE_KEY);
    assertNotNull(caseMilestone);

    caseService.terminateCaseExecution(caseMilestone.getId());

    caseMilestone = queryCaseExecutionByActivityId(MILESTONE_KEY);
    assertNull(caseMilestone);

  }

  protected CaseInstance createCaseInstance(String caseDefinitionKey) {
    return caseService
        .withCaseDefinitionByKey(caseDefinitionKey)
        .create();
  }

  protected CaseExecution queryCaseExecutionByActivityId(String activityId) {
    return caseService
        .createCaseExecutionQuery()
        .activityId(activityId)
        .singleResult();
  }

  protected CaseInstance queryCaseInstanceByKey(String caseDefinitionKey) {
    return caseService
        .createCaseInstanceQuery()
        .caseDefinitionKey(caseDefinitionKey)
        .singleResult();
  }

  protected Task queryTask() {
    return taskService
        .createTaskQuery()
        .singleResult();
  }

}
