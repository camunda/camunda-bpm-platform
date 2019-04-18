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
package org.camunda.bpm.qa.upgrade.scenarios730.job;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * This actually tests migration from 7.0 jobs (where there was no suspension state)
 * to 7.4 (where suspension state is a not-null column).
 *
 * @author Thorben Lindhauer
 */
@ScenarioUnderTest("JobMigrationScenario")
@Origin("7.3.0")
public class JobMigrationScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("createJob.1")
  public void testSuspensionState() {
    // given
    Job job = rule.getManagementService().createJobQuery().jobId(rule.getBuisnessKey()).singleResult();

    // then
    Assert.assertNotNull(job);
    Assert.assertFalse(job.isSuspended());
  }
}
