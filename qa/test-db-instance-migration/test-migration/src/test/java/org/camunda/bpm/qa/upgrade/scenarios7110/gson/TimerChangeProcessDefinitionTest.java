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
package org.camunda.bpm.qa.upgrade.scenarios7110.gson;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Tassilo Weidner
 */
@ScenarioUnderTest("TimerChangeProcessDefinitionScenario")
@Origin("7.11.0")
public class TimerChangeProcessDefinitionTest {

  protected static final Date FIXED_DATE_ONE = new Date(1363608000000L);
  protected static final Date FIXED_DATE_TWO = new Date(1363608500000L);
  protected static final Date FIXED_DATE_THREE = new Date(1363608600000L);

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @After
  public void activateDefinitions() {
    engineRule.getRepositoryService().activateProcessDefinitionByKey("oneTaskProcessTimer_710");
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @ScenarioUnderTest("initTimerChangeProcessDefinition.1")
  @Test
  public void testTimerChangeProcessDefinitionById() {
    // assume
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery()
      .processInstanceBusinessKey("TimerChangeProcessDefinitionScenarioV1")
      .singleResult();

    assertThat(processInstance.isSuspended(), is(false));

    Job job = engineRule.getManagementService().createJobQuery()
      .timers()
      .duedateLowerThan(new Date(1_363_608_001_000L))
      .duedateHigherThan(new Date(1_363_607_999_000L))
      .singleResult();

    ClockUtil.setCurrentTime(FIXED_DATE_ONE);

    // when
    engineRule.getManagementService().executeJob(job.getId());

    processInstance = engineRule.getRuntimeService().createProcessInstanceQuery()
      .processInstanceBusinessKey("TimerChangeProcessDefinitionScenarioV1")
      .singleResult();

    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery()
      .processDefinitionId(processInstance.getProcessDefinitionId())
      .singleResult();

    // then
    assertThat(processInstance.isSuspended(), is(true));
    assertThat(processDefinition.isSuspended(), is(true));
  }

  @ScenarioUnderTest("initTimerChangeProcessDefinition.2")
  @Test
  public void testTimerChangeProcessDefinitionByKeyWithTenantId() {
    // assume
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery()
      .processInstanceBusinessKey("TimerChangeProcessDefinitionScenarioV2")
      .singleResult();

    assertThat(processInstance.isSuspended(), is(false));

    Job job = engineRule.getManagementService().createJobQuery()
      .timers()
      .duedateLowerThan(new Date(1_363_608_501_000L))
      .duedateHigherThan(new Date(1_363_608_499_000L))
      .singleResult();

    ClockUtil.setCurrentTime(FIXED_DATE_TWO);

    // when
    engineRule.getManagementService().executeJob(job.getId());

    processInstance = engineRule.getRuntimeService().createProcessInstanceQuery()
      .processInstanceBusinessKey("TimerChangeProcessDefinitionScenarioV2")
      .singleResult();

    ProcessDefinition processDefinition = engineRule.getRepositoryService().createProcessDefinitionQuery()
      .processDefinitionId(processInstance.getProcessDefinitionId())
      .singleResult();

    // then
    assertThat(processInstance.isSuspended(), is(true));
    assertThat(processDefinition.isSuspended(), is(true));
  }

  @ScenarioUnderTest("initTimerChangeProcessDefinition.3")
  @Test
  public void testTimerChangeProcessDefinitionByKeyWithoutTenantId() {
    ProcessDefinition processDefinitionWithoutTenantId = engineRule.getRepositoryService().createProcessDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimer_710")
      .withoutTenantId()
      .singleResult();

    ProcessDefinition processDefinitionWithTenantId = engineRule.getRepositoryService().createProcessDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimer_710")
      .tenantIdIn("aTenantId")
      .singleResult();

    // assume
    assertThat(processDefinitionWithoutTenantId.isSuspended(), is(false));
    assertThat(processDefinitionWithTenantId.isSuspended(), is(false));

    // given
    Job job = engineRule.getManagementService().createJobQuery()
      .timers()
      .duedateLowerThan(new Date(1_363_608_601_000L))
      .duedateHigherThan(new Date(1_363_608_599_000L))
      .singleResult();

    ClockUtil.setCurrentTime(FIXED_DATE_THREE);

    // when
    engineRule.getManagementService().executeJob(job.getId());

    processDefinitionWithoutTenantId = engineRule.getRepositoryService().createProcessDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimer_710")
      .withoutTenantId()
      .singleResult();

    processDefinitionWithTenantId = engineRule.getRepositoryService().createProcessDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimer_710")
      .tenantIdIn("aTenantId")
      .singleResult();

    // then
    assertThat(processDefinitionWithTenantId.isSuspended(), is(false));
    assertThat(processDefinitionWithoutTenantId.isSuspended(), is(true));
  }

}
