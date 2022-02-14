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
package org.camunda.bpm.qa.largedata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.qa.largedata.util.BatchModificationJobHelper;
import org.camunda.bpm.qa.largedata.util.EngineDataGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class SetJobRetriesAsyncTest {

  protected static final String DATA_PREFIX = SetJobRetriesAsyncTest.class.getSimpleName();
  protected static final int GENERATE_PROCESS_INSTANCES_COUNT = 3000;

  protected ProcessEngineRule engineRule = new ProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchModificationJobHelper helper = new BatchModificationJobHelper(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testRule);

  protected EngineDataGenerator generator;
  protected RuntimeService runtimeService;
  protected ManagementService managementService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();

    // generate data
    generator = new EngineDataGenerator(engineRule.getProcessEngine(), GENERATE_PROCESS_INSTANCES_COUNT, DATA_PREFIX);
    generator.deployDefinitions();
    generator.generateAsyncTaskProcessInstanceData();
  }

  @After
  public void tearDown() {
    helper.removeAllRunningAndHistoricBatches();
  }

  /* See https://jira.camunda.com/browse/CAM-12852 for more details */
  @Test
  public void shouldModifyJobRetriesAsync() {
    // given
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(generator.getAsyncTaskProcessKey())
        .list();
    List<String> processInstanceIds = processInstances.stream()
        .map(ProcessInstance::getId)
        .collect(Collectors.toList());
    int newJobRetriesNumber = 10;
    Batch jobRetriesBatch = engineRule.getManagementService()
        .setJobRetriesAsync(processInstanceIds, (ProcessInstanceQuery) null, newJobRetriesNumber);

    // when
    helper.completeBatch(jobRetriesBatch);

    // then
    List<Job> jobs = managementService.createJobQuery()
        .processDefinitionKey(generator.getAsyncTaskProcessKey())
        .list();
    Set<Integer> retries = jobs.stream().map(Job::getRetries).collect(Collectors.toSet());
    // all the jobs have been updated to the same retry value and that value is 10
    assertThat(retries).hasSize(1).contains(10);
  }

}