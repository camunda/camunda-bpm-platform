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
package org.camunda.bpm.integrationtest.functional.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.functional.transactions.beans.TransactionRollbackDelegate;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test class ensures that when a UserTransaction is explicitly marked as ROLLBACK_ONLY,
 * and this code is executed within a Job, then the transaction is rolled back, and the job
 * execution is marked as failed, reducing the job retries.
 */
@RunWith(Arquillian.class)
public class AsyncJobExecutionWithRollbackTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addClass(TransactionRollbackDelegate.class)
            .addAsResource("org/camunda/bpm/integrationtest/functional/transactions/AsyncJobExecutionWithRollbackTest.transactionRollbackInServiceTask.bpmn20.xml")
            .addAsResource("org/camunda/bpm/integrationtest/functional/transactions/AsyncJobExecutionWithRollbackTest.transactionRollbackInServiceTaskWithCustomRetryCycle.bpmn20.xml")
            .addAsWebInfResource("persistence.xml", "classes/META-INF/persistence.xml");
  }

  @Inject
  private RuntimeService runtimeService;

  @After
  public void cleanUp() {
    for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().list()) {
      runtimeService.deleteProcessInstance(processInstance.getId(), "test ended", true);
    }
  }

  @Test
  public void shouldRollbackTransactionInServiceTask() throws Exception {
    // given
    runtimeService.startProcessInstanceByKey("txRollbackServiceTask");

    // when
    // the job is executed
    waitForJobExecutorToProcessAllJobs(10000);

    // then
    // the job exists with no retries, and an incident is raised
    Job job = managementService.createJobQuery().singleResult();

    assertNotNull(job);
    assertEquals(0, job.getRetries());
    assertNotNull(job.getExceptionMessage());
    assertNotNull(managementService.getJobExceptionStacktrace(job.getId()));
  }

  @Test
  public void shouldRollbackTransactionInServiceTaskWithCustomRetryCycle() throws Exception {
    // given
    runtimeService.startProcessInstanceByKey("txRollbackServiceTaskWithCustomRetryCycle");

    // when
    waitForJobExecutorToProcessAllJobs(10000);

    // then
    // the job exists with no retries, and an incident is raised
    Job job = managementService.createJobQuery().singleResult();

    assertNotNull(job);
    assertEquals(0, job.getRetries());
    assertNotNull(job.getExceptionMessage());
    assertNotNull(managementService.getJobExceptionStacktrace(job.getId()));
  }

}
