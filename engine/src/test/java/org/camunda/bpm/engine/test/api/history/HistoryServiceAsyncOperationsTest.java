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

package org.camunda.bpm.engine.test.api.history;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.AbstractAsyncOperationsTest;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Askar Akhmerov
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoryServiceAsyncOperationsTest extends AbstractAsyncOperationsTest {

  protected static final String TEST_REASON = "test reason";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected TaskService taskService;
  protected List<String> historicProcessInstances;

  @Before
  public void initServices() {
    super.initServices();
    taskService = engineRule.getTaskService();
    prepareData();
  }

  public void prepareData() {
    testRule.deploy("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml");
    startTestProcesses(2);

    for (Task activeTask : taskService.createTaskQuery().list()) {
      taskService.complete(activeTask.getId());
    }

    historicProcessInstances = new ArrayList<String>();
    for (HistoricProcessInstance pi : historyService.createHistoricProcessInstanceQuery().list()) {
      historicProcessInstances.add(pi.getId());
    }
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

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithList() throws Exception {
    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(historicProcessInstances, TEST_REASON);

    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithEmptyList() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);

    //when
    historyService.deleteHistoricProcessInstancesAsync(new ArrayList<String>(), TEST_REASON);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithNonExistingID() throws Exception {
    //given
    ArrayList<String> processInstanceIds = new ArrayList<String>();
    processInstanceIds.add(historicProcessInstances.get(0));
    processInstanceIds.add("aFakeId");

    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(processInstanceIds, TEST_REASON);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    //then
    assertThat(exceptions.size(), is(1));
    assertHistoricBatchExists(testRule);
  }


  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithQueryAndList() throws Exception {
    //given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(historicProcessInstances.get(0));
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(
        historicProcessInstances.subList(1, historicProcessInstances.size()), query, TEST_REASON);
    executeSeedJob(batch);

    //when
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithQuery() throws Exception {
    //given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
        .processInstanceIds(new HashSet<String>(historicProcessInstances));
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(query, TEST_REASON);
    executeSeedJob(batch);

    //when
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertThat(exceptions.size(), is(0));
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithEmptyQuery() throws Exception {
    //expect
    thrown.expect(ProcessEngineException.class);
    //given
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().unfinished();
    //when
    historyService.deleteHistoricProcessInstancesAsync(query, TEST_REASON);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithNonExistingIDAsQuery() throws Exception {
    //given
    ArrayList<String> processInstanceIds = new ArrayList<String>();
    processInstanceIds.add(historicProcessInstances.get(0));
    processInstanceIds.add("aFakeId");
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
        .processInstanceIds(new HashSet(processInstanceIds));

    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(query, TEST_REASON);
    executeSeedJob(batch);
    executeBatchJobs(batch);

    //then
    assertHistoricBatchExists(testRule);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithoutDeleteReason() throws Exception {
    //when
    Batch batch = historyService.deleteHistoricProcessInstancesAsync(historicProcessInstances, null);
    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    //then
    assertThat(exceptions.size(), is(0));
    assertNoHistoryForTasks();
    assertHistoricBatchExists(testRule);
    assertAllHistoricProcessInstancesAreDeleted();
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithNullList() throws Exception {
    thrown.expect(ProcessEngineException.class);
    historyService.deleteHistoricProcessInstancesAsync((List) null, TEST_REASON);
  }

  @Test
  public void testDeleteHistoryProcessInstancesAsyncWithNullQuery() throws Exception {
    thrown.expect(ProcessEngineException.class);
    historyService.deleteHistoricProcessInstancesAsync((HistoricProcessInstanceQuery) null, TEST_REASON);
  }

  protected void assertNoHistoryForTasks() {
    if (!testRule.isHistoryLevelNone()) {
      Assert.assertThat(historyService.createHistoricTaskInstanceQuery().count(), CoreMatchers.is(0L));
    }
  }

  protected void assertAllHistoricProcessInstancesAreDeleted() {
    assertThat(historyService.createHistoricProcessInstanceQuery().count(), is(0L));
  }

}
