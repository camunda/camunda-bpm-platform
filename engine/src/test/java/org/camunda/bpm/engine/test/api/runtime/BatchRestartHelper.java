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
package org.camunda.bpm.engine.test.api.runtime;

import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;


public class BatchRestartHelper extends BatchHelper {

  public BatchRestartHelper(ProcessEngineRule engineRule) {
    super(engineRule);
  }

  public BatchRestartHelper(PluggableProcessEngineTest testCase) {
    super(testCase);
  }

  @Override
  public JobDefinition getExecutionJobDefinition(Batch batch) {
    return getManagementService()
        .createJobDefinitionQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).jobType(Batch.TYPE_PROCESS_INSTANCE_RESTART).singleResult();
  }
  
  
  public void executeJob(Job job) {
    assertNotNull("Job to execute does not exist", job);
    getManagementService().executeJob(job.getId());
  }
}
