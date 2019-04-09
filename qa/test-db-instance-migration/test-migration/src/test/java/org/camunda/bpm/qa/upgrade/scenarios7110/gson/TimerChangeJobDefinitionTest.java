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
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
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
@ScenarioUnderTest("TimerChangeJobDefinitionScenario")
@Origin("7.11.0")
public class TimerChangeJobDefinitionTest {

  protected static final Date FIXED_DATE_ONE = new Date(1363607000000L);
  protected static final Date FIXED_DATE_TWO = new Date(1363607500000L);
  protected static final Date FIXED_DATE_THREE = new Date(1363607600000L);
  protected static final Date FIXED_DATE_FOUR = new Date(1363607700000L);

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @After
  public void activateDefinitions() {
    engineRule.getManagementService().activateJobDefinitionByProcessDefinitionKey("oneTaskProcessTimerJob_710", true);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @ScenarioUnderTest("initTimerChangeJobDefinition.1")
  @Test
  public void testTimerChangeJobDefinitionByIdWithJobsIncluded() {
    Job job = engineRule.getManagementService().createJobQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .withoutTenantId()
      .singleResult();

    JobDefinition jobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .withoutTenantId()
      .singleResult();

    // assume
    assertThat(job.isSuspended(), is(false));
    assertThat(jobDefinition.isSuspended(), is(false));

    Job timerJob = engineRule.getManagementService().createJobQuery()
      .timers()
      .duedateLowerThan(new Date(1_363_607_001_000L))
      .duedateHigherThan(new Date(1_363_606_999_000L))
      .singleResult();

    ClockUtil.setCurrentTime(FIXED_DATE_ONE);

    // when
    engineRule.getManagementService().executeJob(timerJob.getId());

    job = engineRule.getManagementService().createJobQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .withoutTenantId()
      .singleResult();

    jobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .withoutTenantId()
      .singleResult();

    // then
    assertThat(job.isSuspended(), is(true));
    assertThat(jobDefinition.isSuspended(), is(true));
  }

  @ScenarioUnderTest("initTimerChangeJobDefinition.2")
  @Test
  public void testTimerChangeProcessDefinitionById() {
    JobDefinition jobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .withoutTenantId()
      .singleResult();

    // assume
    assertThat(jobDefinition.isSuspended(), is(false));

    Job timerJob = engineRule.getManagementService().createJobQuery()
      .timers()
      .duedateLowerThan(new Date(1_363_607_501_000L))
      .duedateHigherThan(new Date(1_363_607_499_000L))
      .singleResult();

    ClockUtil.setCurrentTime(FIXED_DATE_TWO);

    // when
    engineRule.getManagementService().executeJob(timerJob.getId());

    jobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .withoutTenantId()
      .singleResult();

    // then
    assertThat(jobDefinition.isSuspended(), is(true));
  }

  @ScenarioUnderTest("initTimerChangeJobDefinition.3")
  @Test
  public void testTimerChangeJobDefinitionByKeyWithTenantId() {
    JobDefinition jobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .tenantIdIn("aTenantId")
      .singleResult();

    // assume
    assertThat(jobDefinition.isSuspended(), is(false));

    Job timerJob = engineRule.getManagementService().createJobQuery()
      .timers()
      .duedateLowerThan(new Date(1_363_607_601_000L))
      .duedateHigherThan(new Date(1_363_607_599_000L))
      .singleResult();

    ClockUtil.setCurrentTime(FIXED_DATE_THREE);

    // when
    engineRule.getManagementService().executeJob(timerJob.getId());

    jobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .tenantIdIn("aTenantId")
      .singleResult();

    // then
    assertThat(jobDefinition.isSuspended(), is(true));
  }

  @ScenarioUnderTest("initTimerChangeJobDefinition.4")
  @Test
  public void testTimerChangeJobDefinitionByKeyWithoutTenantId() {
    JobDefinition jobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .withoutTenantId()
      .singleResult();

    // assume
    assertThat(jobDefinition.isSuspended(), is(false));

    Job timerJob = engineRule.getManagementService().createJobQuery()
      .timers()
      .duedateLowerThan(new Date(1_363_607_701_000L))
      .duedateHigherThan(new Date(1_363_607_699_000L))
      .singleResult();

    ClockUtil.setCurrentTime(FIXED_DATE_FOUR);

    // when
    engineRule.getManagementService().executeJob(timerJob.getId());

    jobDefinition = engineRule.getManagementService().createJobDefinitionQuery()
      .processDefinitionKey("oneTaskProcessTimerJob_710")
      .withoutTenantId()
      .singleResult();

    // then
    assertThat(jobDefinition.isSuspended(), is(true));
  }

}
