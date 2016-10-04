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

package org.camunda.bpm.engine.test.api.runtime;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.AbstractAsyncOperationsTest;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * @author Askar Akhmerov
 */
public class RuntimeServiceAsyncOperationsTest extends AbstractAsyncOperationsTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
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

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithList() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithListOnly() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNonExistingId() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    processIds.add("unknown");

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processIds, null, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    List<Exception> exceptions = executeBatchJobs(batch);

    // then
    assertEquals(1, exceptions.size());

    Exception e = exceptions.get(0);
    assertTrue(e.getMessage().startsWith("No process instance found for id 'unknown'"));

    assertThat(managementService.createJobQuery().withException().list().size(), is(1));

    processIds.remove("unknown");
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullList() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    runtimeService.deleteProcessInstancesAsync(null, null, TESTING_INSTANCE_DELETE);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithEmptyList() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    runtimeService.deleteProcessInstancesAsync(new ArrayList<String>(), null, TESTING_INSTANCE_DELETE);

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQuery() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery().processInstanceIds(new HashSet<String>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(null, processInstanceQuery, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQueryOnly() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery().processInstanceIds(new HashSet<String>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(processInstanceQuery, TESTING_INSTANCE_DELETE);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, TESTING_INSTANCE_DELETE, testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQueryWithoutDeleteReason() throws Exception {
    // given
    List<String> processIds = startTestProcesses(2);
    ProcessInstanceQuery processInstanceQuery = runtimeService
        .createProcessInstanceQuery().processInstanceIds(new HashSet<String>(processIds));

    // when
    Batch batch = runtimeService.deleteProcessInstancesAsync(null, processInstanceQuery, null);

    executeSeedJob(batch);
    executeBatchJobs(batch);

    // then
    assertHistoricTaskDeletionPresent(processIds, "deleted", testRule);
    assertHistoricBatchExists(testRule);
    assertProcessInstancesAreDeleted();
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullQueryParameter() throws Exception {
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    runtimeService.deleteProcessInstancesAsync(null, null, TESTING_INSTANCE_DELETE);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithInvalidQueryParameter() throws Exception {
    // given
    startTestProcesses(2);
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey("invalid");

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");

    // when
    runtimeService.deleteProcessInstancesAsync(null, query, TESTING_INSTANCE_DELETE);
  }

  protected void assertProcessInstancesAreDeleted() {
    assertThat(runtimeService.createProcessInstanceQuery().list().size(), is(0));
  }

}
