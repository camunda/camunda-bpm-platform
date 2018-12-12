/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DeploymentAwareJobExecutorForOracleTest {

  protected ProcessEngineBootstrapRule deploymentAwareBootstrapRule = new ProcessEngineBootstrapRule() {
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {
      configuration.setJobExecutorDeploymentAware(true);
      return configuration;
    }
  };
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(deploymentAwareBootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(deploymentAwareBootstrapRule).around(engineRule).around(testRule);
  
  @Test
  public void testFindAcquirableJobsWhen0InstancesDeployed() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));

    // then
    findAcquirableJobs();
  }

  @Test
  public void testFindAcquirableJobsWhen1InstanceDeployed() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));
    // when
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    // then
    findAcquirableJobs();
  }

  @Test
  public void testFindAcquirableJobsWhen1000InstancesDeployed() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));
    // when
    for (int i=0; i<1000; i++) {
      testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    }
    // then
    findAcquirableJobs();
  }

  @Test
  public void testFindAcquirableJobsWhen1001InstancesDeployed() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));
    // when
    for (int i=0; i<1001; i++) {
      testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    }
    // then
    findAcquirableJobs();
  }

  @Test
  public void testFindAcquirableJobsWhen2000InstancesDeployed() {
    // given
    Assume.assumeTrue(engineRule.getProcessEngineConfiguration().getDatabaseType().equals("oracle"));
    // when
    for (int i=0; i<2000; i++) {
      testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    }
    // then
    findAcquirableJobs();
  }

  protected List<JobEntity> findAcquirableJobs() {
    return engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired().execute(new Command<List<JobEntity>>() {

      @Override
      public List<JobEntity> execute(CommandContext commandContext) {
        return commandContext
          .getJobManager()
          .findNextJobsToExecute(new Page(0, 100));
      }
    });
  }
}
