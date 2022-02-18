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
package org.camunda.bpm.qa.upgrade.scenarios7170.pvm;

import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("AsyncJoinScenario")
@Origin("7.16.0")
public class AsyncJoinTest {

  @Rule
  public UpgradeTestRule engineRule = new UpgradeTestRule();

  ManagementService managementService;

  @Before
  public void assignServices() {
    managementService = engineRule.getManagementService();
  }

  @Test
  @ScenarioUnderTest("asyncJoinParallel.1")
  public void shouldCompleteWithConcurrentExecution_ParallelGateway() {
    // given
    JobQuery jobQuery = engineRule.jobQuery();
    AtomicReference<String> jobIdNotifyListener = new AtomicReference<>();
    AtomicReference<String> jobIdActivityEnd = new AtomicReference<>();
    jobQuery.list().forEach(job -> {
      if ("transition-notify-listener-take$Flow_0wno03o".equals(((JobEntity)job).getJobHandlerConfigurationRaw())) {
        jobIdNotifyListener.set(job.getId());
      } else if ("activity-end".equals(((JobEntity)job).getJobHandlerConfigurationRaw())) {
        jobIdActivityEnd.set(job.getId());
      }
    });

    managementService.executeJob(jobIdActivityEnd.get());
    
    // when
    managementService.executeJob(jobIdNotifyListener.get());

    // then
    Assertions.assertThat(jobQuery.count()).isEqualTo(0);
    Assertions.assertThat(engineRule.historicProcessInstance().getState())
        .isEqualTo("COMPLETED");
  }

  @Test
  @ScenarioUnderTest("asyncJoinInclusive.1")
  public void shouldCompleteWithConcurrentExecution_InclusiveGateway() {
    // given
    JobQuery jobQuery = engineRule.jobQuery();
    AtomicReference<String> jobIdNotifyListener = new AtomicReference<>();
    AtomicReference<String> jobIdActivityEnd = new AtomicReference<>();
    jobQuery.list().forEach(job -> {
      if ("transition-notify-listener-take$Flow_0wno03o".equals(((JobEntity)job).getJobHandlerConfigurationRaw())) {
        jobIdNotifyListener.set(job.getId());
      } else if ("activity-end".equals(((JobEntity)job).getJobHandlerConfigurationRaw())) {
        jobIdActivityEnd.set(job.getId());
      }
    });

    managementService.executeJob(jobIdActivityEnd.get());
    
    // when
    managementService.executeJob(jobIdNotifyListener.get());

    // then
    Assertions.assertThat(jobQuery.count()).isEqualTo(0);
    Assertions.assertThat(engineRule.historicProcessInstance().getState())
        .isEqualTo("COMPLETED");
  }
}
