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
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrateSuspendedInstanceTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;

  @Before
  public void setup() {
    runtimeService = engineRule.getRuntimeService();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();
  }

  @Test
  public void shouldNotExecuteTimerJobAfterMigrateSuspendedInstance() {
    // given
    // process instance with single user task
    BpmnModelInstance modelInstanceVersion1 = Bpmn.createExecutableProcess("processId")
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .done();
    ProcessDefinition definition1 = testHelper.deployAndGetDefinition(modelInstanceVersion1);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceById(definition1.getId());
    // suspend instance, jobs belonging to suspended instances should not execute
    runtimeService.suspendProcessInstanceById(processInstance1.getId());

    // version two has a cycle timer
    BpmnModelInstance modelInstanceVersion2 = Bpmn.createExecutableProcess("processId")
        .startEvent()
        .userTask("userTask")
        .endEvent()
        .moveToActivity("userTask")
        .boundaryEvent()
        .cancelActivity(false)
        .timerWithCycle("R3/PT5S")
        .endEvent()
        .done();
    ProcessDefinition definition2 = testHelper.deployAndGetDefinition(modelInstanceVersion2);

    // migrate process instance to version 2
    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(definition1.getId(), definition2.getId())
        .mapEqualActivities()
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance1);

    // then
    Job job = managementService.createJobQuery().singleResult();
    assertThat(job.isSuspended()).isTrue();
    List<Incident> incidents = runtimeService.createIncidentQuery().list();
    assertThat(incidents).isEmpty();
  }
}
