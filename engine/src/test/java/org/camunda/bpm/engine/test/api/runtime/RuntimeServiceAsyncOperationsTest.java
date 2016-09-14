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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.runners.MethodSorters;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public class RuntimeServiceAsyncOperationsTest {
  public static final String TESTING_INSTANCE_DELETION = "testing instance deletion";
  public static final String ONE_TASK_PROCESS = "oneTaskProcess";

  public ProcessEngineRule engineRule = new ProcessEngineRule(true);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() {
    org.camunda.bpm.engine.repository.Deployment deployment = engineRule.getRepositoryService().createDeploymentQuery().singleResult();
    engineRule.manageDeployment(deployment);
  }

  @After
  public void cleanBatch() {
    HistoricBatch historicBatch = engineRule.getHistoryService().createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      engineRule.getHistoryService().deleteHistoricBatch(
          historicBatch.getId());
    }
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithList() throws Exception {

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    ProcessInstance processInstance2 = engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count(),is(2l));


    List<String> processInstanceIds = Arrays.asList(processInstance.getId(), processInstance2.getId());
    engineRule.getRuntimeService().deleteProcessInstancesAsync(processInstanceIds,TESTING_INSTANCE_DELETION);

    engineRule.getManagementService().executeJob(engineRule.getManagementService().createJobQuery().singleResult().getId());
    List<Job> list = engineRule.getManagementService().createJobQuery().list();
    assertThat(list.size(),is(3));
    for(Job job: list) {
      engineRule.getManagementService().executeJob(job.getId());
    }

    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory())) {

      HistoricTaskInstance historicTaskInstance = engineRule.getHistoryService()
          .createHistoricTaskInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();

      assertThat(historicTaskInstance.getDeleteReason(), is(TESTING_INSTANCE_DELETION));
      assertThat(engineRule.getHistoryService()
          .createHistoricTaskInstanceQuery().count(),is(2l));
    }

    if(ProcessEngineConfiguration.HISTORY_FULL.equals(engineRule.getProcessEngineConfiguration().getHistory())) {
      assertThat(engineRule.getHistoryService().createHistoricBatchQuery().count(), is(1l));
    }
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().list().size(),is(0));
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNonExistingId() throws Exception {
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    ProcessInstance processInstance2 = engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count(),is(2l));


    List<String> listWithFake = Arrays.asList(processInstance.getId(), processInstance2.getId(), "fake");
    engineRule.getRuntimeService().deleteProcessInstancesAsync(listWithFake,TESTING_INSTANCE_DELETION);
    engineRule.getManagementService().executeJob(engineRule.getManagementService().createJobQuery().singleResult().getId());
    List<Job> list = engineRule.getManagementService().createJobQuery().list();
    assertThat(list.size(),is(4));

    for(Job job: list) {
      try {
        engineRule.getManagementService().executeJob(job.getId());
      } catch (Exception e) {
        if (!e.getMessage().startsWith("No process instance found for id 'fake'")) {
          throw new RuntimeException("unexpected exception");
        }
      }
    }

    assertThat(engineRule.getManagementService().createJobQuery().withException().list().size(),is(1));

    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory())) {

      HistoricTaskInstance historicTaskInstance = engineRule.getHistoryService()
          .createHistoricTaskInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();

      assertThat(historicTaskInstance.getDeleteReason(), is(TESTING_INSTANCE_DELETION));
      assertThat(engineRule.getHistoryService()
          .createHistoricTaskInstanceQuery().count(),is(2l));
    }

    if(ProcessEngineConfiguration.HISTORY_FULL.equals(engineRule.getProcessEngineConfiguration().getHistory())) {
      assertThat(engineRule.getHistoryService().createHistoricBatchQuery().count(), is(1l));
    }
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().list().size(),is(0));

    engineRule.getManagementService().deleteBatch(
        engineRule.getManagementService().createBatchQuery().singleResult().getId(),true);
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullList() throws Exception {
    engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count(),is(2l));

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is null");
    engineRule.getRuntimeService().deleteProcessInstancesAsync((List<String>)null,TESTING_INSTANCE_DELETION);

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithemptyList() throws Exception {
    engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count(),is(2l));

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");
    engineRule.getRuntimeService().deleteProcessInstancesAsync(new ArrayList<String>(),TESTING_INSTANCE_DELETION);

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQuery() throws Exception {
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery()
        .processDefinitionKey(ONE_TASK_PROCESS).count(),is(2l));


    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService()
        .createProcessInstanceQuery().processInstanceIds(
            new HashSet<String>(Arrays.asList(processInstance.getId(),processInstance1.getId())));
    engineRule.getRuntimeService().deleteProcessInstancesAsync(processInstanceQuery,TESTING_INSTANCE_DELETION);

    engineRule.getManagementService().executeJob(engineRule.getManagementService().createJobQuery().singleResult().getId());
    List<Job> list = engineRule.getManagementService().createJobQuery().list();
    assertThat(list.size(),is(3));
    for(Job job: list) {
      engineRule.getManagementService().executeJob(job.getId());
    }

    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory())) {

      HistoricTaskInstance historicTaskInstance = engineRule.getHistoryService()
          .createHistoricTaskInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();

      assertThat(historicTaskInstance.getDeleteReason(), is(TESTING_INSTANCE_DELETION));
      assertThat(engineRule.getHistoryService()
          .createHistoricTaskInstanceQuery().count(),is(2l));
    }

    if(ProcessEngineConfiguration.HISTORY_FULL.equals(engineRule.getProcessEngineConfiguration().getHistory())) {
      assertThat(engineRule.getHistoryService().createHistoricBatchQuery().count(), is(1l));
    }
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().list().size(),is(0));
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithQueryWithoutDeletionReason() throws Exception {
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery()
        .processDefinitionKey(ONE_TASK_PROCESS).count(),is(2l));


    ProcessInstanceQuery processInstanceQuery = engineRule.getRuntimeService()
        .createProcessInstanceQuery().processInstanceIds(
            new HashSet<String>(Arrays.asList(processInstance.getId(),processInstance1.getId())));
    engineRule.getRuntimeService().deleteProcessInstancesAsync(processInstanceQuery,null);

    engineRule.getManagementService().executeJob(engineRule.getManagementService().createJobQuery().singleResult().getId());
    List<Job> list = engineRule.getManagementService().createJobQuery().list();
    assertThat(list.size(),is(3));
    for(Job job: list) {
      engineRule.getManagementService().executeJob(job.getId());
    }

    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(engineRule.getProcessEngineConfiguration().getHistory())) {

      HistoricTaskInstance historicTaskInstance = engineRule.getHistoryService()
          .createHistoricTaskInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();

      assertThat(historicTaskInstance.getDeleteReason(), is(TaskEntity.DELETE_REASON_DELETED));
      assertThat(engineRule.getHistoryService()
          .createHistoricTaskInstanceQuery().count(),is(2l));
    }

    if(ProcessEngineConfiguration.HISTORY_FULL.equals(engineRule.getProcessEngineConfiguration().getHistory())) {
      assertThat(engineRule.getHistoryService().createHistoricBatchQuery().count(), is(1l));
    }
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().list().size(),is(0));
  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithNullQueryParameter() throws Exception {
    engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count(),is(2l));

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceQuery is null");
    engineRule.getRuntimeService().deleteProcessInstancesAsync((ProcessInstanceQuery)null,TESTING_INSTANCE_DELETION);

  }

  @Deployment(resources={
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  @Test
  public void testDeleteProcessInstancesAsyncWithInvalidQueryParameter() throws Exception {
    engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    engineRule.getRuntimeService().startProcessInstanceByKey(ONE_TASK_PROCESS);
    assertThat(engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionKey(ONE_TASK_PROCESS).count(),is(2l));

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("processInstanceIds is empty");
    ProcessInstanceQuery query = engineRule.getRuntimeService().createProcessInstanceQuery()
        .processInstanceBusinessKey("invalid");
    engineRule.getRuntimeService().deleteProcessInstancesAsync(query,TESTING_INSTANCE_DELETION);

  }
}
