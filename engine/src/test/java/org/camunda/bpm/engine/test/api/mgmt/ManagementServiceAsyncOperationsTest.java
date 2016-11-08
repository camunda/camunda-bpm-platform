/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.api.mgmt;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.api.AbstractAsyncOperationsTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Askar Akhmerov
 */
public class ManagementServiceAsyncOperationsTest extends AbstractAsyncOperationsTest {
  protected static final int RETRIES = 5;
  protected static final java.lang.String TEST_PROCESS = "exceptionInJobExecution";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected List<String> processInstanceIds;
  protected List<String> ids;

  @Before
  public void initServices() {
    super.initServices();
    prepareData();
  }

  public void prepareData() {
    testRule.deploy("org/camunda/bpm/engine/test/api/mgmt/ManagementServiceTest.testGetJobExceptionStacktrace.bpmn20.xml");
    processInstanceIds = startTestProcesses(2);
    ids = getAllJobIds();
  }

  @After
  public void cleanBatch() {
    Batch batch = managementService.createBatchQuery().singleResult();
    if (batch != null) {
      managementService.deleteBatch(batch.getId(), true);
    }

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }

  protected List<String> getAllJobIds() {
    ArrayList<String> result = new ArrayList<String>();
    for (Job job : managementService.createJobQuery().list()) {
      if (job.getProcessInstanceId() != null) {
        result.add(job.getId());
      }
    }
    return result;
  }

  protected List<String> startTestProcesses(int numberOfProcesses) {
    ArrayList<String> ids = new ArrayList<String>();

    for (int i = 0; i < numberOfProcesses; i++) {
      ids.add(runtimeService.startProcessInstanceByKey(TEST_PROCESS).getProcessInstanceId());
    }

    return ids;
  }

  protected void assertRetries(List<String> allJobIds, int i) {
    for (String id : allJobIds) {
      Assert.assertThat(managementService.createJobQuery().jobId(id).singleResult().getRetries(), is(i));
    }
  }

  @Test
  public void testSetJobsRetryAsyncWithJobList() throws Exception {
    //when
    Batch batch = managementService.setJobRetriesAsync(ids, RETRIES);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithProcessList() throws Exception {
    //when
    Batch batch = managementService.setJobRetriesAsync(processInstanceIds, (ProcessInstanceQuery) null, RETRIES);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithEmptyJobList() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //when
    managementService.setJobRetriesAsync(new ArrayList<String>(), RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithEmptyProcessList() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //when
    managementService.setJobRetriesAsync(new ArrayList<String>(), (ProcessInstanceQuery) null, RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithNonExistingJobID() throws Exception {
    //given
    ids.add("aFake");

    //when
    Batch batch = managementService.setJobRetriesAsync(ids, RETRIES);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    //then
    assertThat(exceptions.size(), is(1));
    assertRetries(getAllJobIds(), RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithNonExistingProcessID() throws Exception {
    //given
    processInstanceIds.add("aFake");

    //when
    Batch batch = managementService.setJobRetriesAsync(processInstanceIds, (ProcessInstanceQuery) null, RETRIES);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    //then
    assertThat(exceptions.size(), is(0));
    assertRetries(getAllJobIds(), RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithJobQueryAndList() throws Exception {
    //given
    List<String> extraPi = startTestProcesses(1);
    JobQuery query = managementService.createJobQuery().processInstanceId(extraPi.get(0));

    //when
    Batch batch = managementService.setJobRetriesAsync(ids, query, RETRIES);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertRetries(getAllJobIds(), RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithProcessQueryAndList() throws Exception {
    //given
    List<String> extraPi = startTestProcesses(1);
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceId(extraPi.get(0));

    //when
    Batch batch = managementService.setJobRetriesAsync(processInstanceIds, query, RETRIES);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertRetries(getAllJobIds(), RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithJobQuery() throws Exception {
    //given
    JobQuery query = managementService.createJobQuery();

    //when
    Batch batch = managementService.setJobRetriesAsync(query, RETRIES);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithProcessQuery() throws Exception {
    //given
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    //when
    Batch batch = managementService.setJobRetriesAsync(null, query, RETRIES);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertRetries(ids, RETRIES);
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testSetJobsRetryAsyncWithEmptyJobQuery() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //given
    JobQuery query = managementService.createJobQuery().suspended();

    //when
    managementService.setJobRetriesAsync(query, RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithEmptyProcessQuery() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //given
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().suspended();

    //when
    managementService.setJobRetriesAsync(null, query, RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithNonExistingIDAsJobQuery() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //given
    JobQuery query = managementService.createJobQuery().jobId(ids.get(0)).jobId("aFake");

    //when
    managementService.setJobRetriesAsync(query, RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithNonExistingIDAsProcessQuery() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //given
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processInstanceId("aFake");

    //when
    managementService.setJobRetriesAsync(null, query, RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithNullJobList() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //when
    managementService.setJobRetriesAsync((ArrayList) null, RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithNullJobQuery() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //when
    managementService.setJobRetriesAsync((JobQuery) null, RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithNullProcessQuery() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //when
    managementService.setJobRetriesAsync(null, (ProcessInstanceQuery) null, RETRIES);
  }

  @Test
  public void testSetJobsRetryAsyncWithNegativeRetries() throws Exception {
    //given
    JobQuery query = managementService.createJobQuery();

    //when
    thrown.expect(ProcessEngineException.class);
    managementService.setJobRetriesAsync(query, -1);
  }
}
