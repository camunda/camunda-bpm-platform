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
package org.camunda.bpm.engine.test.api.authorization.batch.creation;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.junit.Test;
import org.junit.runners.Parameterized;

public class SetJobRetriesBatchAuthorizationTest extends BatchCreationAuthorizationTest {

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withoutAuthorizations()
            .failsDueToRequired(
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE),
                grant(Resources.BATCH, "batchId", "userId", BatchPermissions.CREATE_BATCH_SET_JOB_RETRIES)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "batchId", "userId", Permissions.CREATE)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.BATCH, "batchId", "userId", BatchPermissions.CREATE_BATCH_SET_JOB_RETRIES)
            ).succeeds()
    );
  }

  @Test
  public void testBatchSetJobRetriesByJobs() {
    //given
    List<String> jobIds = setupFailedJobs();
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("batchId", "*")
        .start();

    // when

    managementService.setJobRetriesAsync(jobIds, 5);

    // then
    authRule.assertScenario(scenario);
  }

  @Test
  public void testBatchSetJobRetriesByProcesses() {
    //given
    setupFailedJobs();
    List<String> processInstanceIds = Collections.singletonList(processInstance.getId());
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("batchId", "*")
        .start();

    // when

    managementService.setJobRetriesAsync(processInstanceIds, (ProcessInstanceQuery) null, 5);

    // then
    authRule.assertScenario(scenario);
  }

  protected List<String> setupFailedJobs() {
    List<String> jobIds = new ArrayList<>();

    Deployment deploy = testHelper.deploy(JOB_EXCEPTION_DEFINITION_XML);
    ProcessDefinition sourceDefinition = engineRule.getRepositoryService()
        .createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
    processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    List<Job> jobs = managementService.createJobQuery().processInstanceId(processInstance.getId()).list();
    for (Job job : jobs) {
      jobIds.add(job.getId());
    }
    return jobIds;
  }

}
