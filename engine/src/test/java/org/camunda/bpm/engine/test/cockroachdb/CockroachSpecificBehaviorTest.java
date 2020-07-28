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
package org.camunda.bpm.engine.test.cockroachdb;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Let's add CRDB-specific behavior tests here
 */
public class CockroachSpecificBehaviorTest {
  
  @Test
  public void testJobAcquisition() {
    // TODO: test the scenario described in CompetingJobAcquisitionTest with TX retries.
    fail("Test job acqusition with > 0 command retries for CRDB");
  } 
  
  @Test
  public void testExternalTaskFetchAndLock() {
    fail("Test fetch and lock with > 0 command retries for CRDB");
  }

  @Test
  public void shouldRetryTxOnHistoricOptimisticLockingException() {
    fail("Test tx retries after a competing tx throws a historic OLE.");
  }

  @Test
  public void shouldRetryTxToReconfigureHistoryCleanupJobsOnOle() {
    fail("Test HistoryCleanupJobs reconfiguration with >0 command retried on CRDB,");
  }

  @Test
  public void shouldRetryAcquistionJobTxAfterJobSuspensionOLE() {
    fail("Test AcquisitionJobCommand retries on CRDB after a concurrent JobSuspensionCommand causes an OLE. " +
      "See ConcurrentJobExecutorTest#testCompletingSuspendJobDuringAcquisition()");
  }

  @Test
  public void shouldRetryJobExecutionTxAfterJobPriorityOLE() {
    fail("Test Job Execution retries on CRDB after a concurrent job changes job priorities and causes an OLE. " +
      "See ConcurrentJobExecutorTest#testCompletingUpdateJobDefinitionPriorityDuringExecution()");
  }

  @Test
  public void shouldRetryConcurrentHistoryCleanupJobAfterFirstJobSucceeds() {
    fail("Test Job Execution retries on CRDB after a concurrent HistoruCleanupJob causes an OLE on " +
      "an existing HistoryCleanupJob. See ConcurrentHistoryCleanupTest#testRunTwoHistoryCleanups()");
  }

  @Test
  public void shouldRetryConcurrentExclusiveMessageCorrelation() {
    fail("Test Job Execution retries on two concurrent exclusive message correlations. " +
      "See CompetingMessageCorrelationTest#testConcurrentExclusiveCorrelation().");
  }

  @Test
  public void shouldRetryConcurrentExclusiveMessageCorrelationOnDifferentExecutions() {
    fail("Test Job Execution retries on two concurrent exclusive message correlations on different executions. " +
      "See CompetingMessageCorrelationTest#testConcurrentExclusiveCorrelationToDifferentExecutions().");
  }
}