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
package org.camunda.bpm.engine.test.history;

import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByActivityId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByDeploymentId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByExecutionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByJobDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByJobDueDate;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByJobId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByJobPriority;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByJobRetries;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByProcessDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByProcessDefinitionKey;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByProcessInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByTimestamp;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogPartiallyByOccurence;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil;
import org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.NullTolerantComparator;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricJobLogQueryTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQuery() {
    runtimeService.startProcessInstanceByKey("process");
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByLogId() {
    runtimeService.startProcessInstanceByKey("process");
    String logId = historyService.createHistoricJobLogQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().logId(logId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidLogId() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().logId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.logId(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByJobId() {
    runtimeService.startProcessInstanceByKey("process");
    String jobId = managementService.createJobQuery().singleResult().getId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId(jobId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidJobId() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.jobId(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByJobExceptionMessage() {
    runtimeService.startProcessInstanceByKey("process");
    String jobId = managementService.createJobQuery().singleResult().getId();
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobExceptionMessage(FailingDelegate.EXCEPTION_MESSAGE);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidJobExceptionMessage() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobExceptionMessage("invalid");

    verifyQueryResults(query, 0);

    try {
      query.jobExceptionMessage(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByJobDefinitionId() {
    runtimeService.startProcessInstanceByKey("process");
    String jobDefinitionId = managementService.createJobQuery().singleResult().getJobDefinitionId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobDefinitionId(jobDefinitionId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidJobDefinitionId() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobDefinitionId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.jobDefinitionId(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByJobDefinitionType() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobDefinitionType(AsyncContinuationJobHandler.TYPE);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidJobDefinitionType() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobDefinitionType("invalid");

    verifyQueryResults(query, 0);

    try {
      query.jobDefinitionType(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByJobDefinitionConfiguration() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobDefinitionConfiguration(MessageJobDeclaration.ASYNC_BEFORE);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidJobDefinitionConfiguration() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobDefinitionConfiguration("invalid");

    verifyQueryResults(query, 0);

    try {
      query.jobDefinitionConfiguration(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByActivityId() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().activityIdIn("serviceTask");

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidActivityId() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().activityIdIn("invalid");

    verifyQueryResults(query, 0);

    String[] nullValue = null;

    try {
      query.activityIdIn(nullValue);
      fail();
    } catch (Exception e) {
    }

    String[] activityIdsContainsNull = {"a", null, "b"};

    try {
      query.activityIdIn(activityIdsContainsNull);
      fail();
    } catch (Exception e) {
    }

    String[] activityIdsContainsEmptyString = {"a", "", "b"};

    try {
      query.activityIdIn(activityIdsContainsEmptyString);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByExecutionId() {
    runtimeService.startProcessInstanceByKey("process");
    String executionId = managementService.createJobQuery().singleResult().getExecutionId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().executionIdIn(executionId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidExecutionId() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().executionIdIn("invalid");

    verifyQueryResults(query, 0);

    String[] nullValue = null;

    try {
      query.executionIdIn(nullValue);
      fail();
    } catch (Exception e) {
    }

    String[] executionIdsContainsNull = {"a", null, "b"};

    try {
      query.executionIdIn(executionIdsContainsNull);
      fail();
    } catch (Exception e) {
    }

    String[] executionIdsContainsEmptyString = {"a", "", "b"};

    try {
      query.executionIdIn(executionIdsContainsEmptyString);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByProcessInstanceId() {
    runtimeService.startProcessInstanceByKey("process");
    String processInstanceId = managementService.createJobQuery().singleResult().getProcessInstanceId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().processInstanceId(processInstanceId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidProcessInstanceId() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().processInstanceId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.processInstanceId(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByProcessDefinitionId() {
    runtimeService.startProcessInstanceByKey("process");
    String processDefinitionId = managementService.createJobQuery().singleResult().getProcessDefinitionId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().processDefinitionId(processDefinitionId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidProcessDefinitionId() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().processDefinitionId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.processDefinitionId(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByProcessDefinitionKey() {
    runtimeService.startProcessInstanceByKey("process");
    String processDefinitionKey = managementService.createJobQuery().singleResult().getProcessDefinitionKey();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().processDefinitionKey(processDefinitionKey);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidProcessDefinitionKey() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().processDefinitionKey("invalid");

    verifyQueryResults(query, 0);

    try {
      query.processDefinitionKey(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByDeploymentId() {
    runtimeService.startProcessInstanceByKey("process");
    String deploymentId = managementService.createJobQuery().singleResult().getDeploymentId();

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().deploymentId(deploymentId);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidDeploymentId() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().deploymentId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.deploymentId(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment
  public void testQueryByJobPriority() {
    // given 5 process instances with 5 jobs
    List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();

    for (int i = 0; i < 5; i++) {
      processInstances.add(runtimeService.startProcessInstanceByKey("process",
          Variables.createVariables().putValue("priority", i)));
    }

    // then the creation logs can be filtered by priority of the jobs
    // (1) lower than or equal a priority
    List<HistoricJobLog> jobLogs = historyService.createHistoricJobLogQuery()
        .jobPriorityLowerThanOrEquals(2L)
        .orderByJobPriority()
        .asc()
        .list();

    assertEquals(3, jobLogs.size());
    for (HistoricJobLog log : jobLogs) {
      assertTrue(log.getJobPriority() <= 2);
    }

    // (2) higher than or equal a given priorty
    jobLogs = historyService.createHistoricJobLogQuery()
        .jobPriorityHigherThanOrEquals(3L)
        .orderByJobPriority()
        .asc()
        .list();

    assertEquals(2, jobLogs.size());
    for (HistoricJobLog log : jobLogs) {
      assertTrue(log.getJobPriority() >= 3);
    }

    // (3) lower and higher than or equal
    jobLogs = historyService.createHistoricJobLogQuery()
        .jobPriorityHigherThanOrEquals(1L)
        .jobPriorityLowerThanOrEquals(3L)
        .orderByJobPriority()
        .asc()
        .list();

    assertEquals(3, jobLogs.size());
    for (HistoricJobLog log : jobLogs) {
      assertTrue(log.getJobPriority() >= 1 && log.getJobPriority() <= 3);
    }

    // (4) lower and higher than or equal are disjunctive
    jobLogs = historyService.createHistoricJobLogQuery()
        .jobPriorityHigherThanOrEquals(3)
        .jobPriorityLowerThanOrEquals(1)
        .orderByJobPriority()
        .asc()
        .list();
    assertEquals(0, jobLogs.size());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByCreationLog() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().creationLog();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByFailureLog() {
    runtimeService.startProcessInstanceByKey("process");
    String jobId = managementService.createJobQuery().singleResult().getId();
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().failureLog();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryBySuccessLog() {
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.executeJob(jobId);

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().successLog();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByDeletionLog() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    runtimeService.deleteProcessInstance(processInstanceId, null);

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().deletionLog();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQuerySorting() {
    for (int i = 0; i < 10; i++) {
      runtimeService.startProcessInstanceByKey("process");
    }

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    // asc
    query
      .orderByTimestamp()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByTimestamp());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobId()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByJobId());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobDefinitionId()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByJobDefinitionId());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobDueDate()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByJobDueDate());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobRetries()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByJobRetries());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByActivityId()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByActivityId());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByExecutionId()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByExecutionId());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByProcessInstanceId()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByProcessInstanceId());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByProcessDefinitionId()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByProcessDefinitionId());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByProcessDefinitionKey()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByProcessDefinitionKey(processEngine));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByDeploymentId()
      .asc();
    verifyQueryWithOrdering(query, 10, historicJobLogByDeploymentId());

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobPriority()
      .asc();

    verifyQueryWithOrdering(query, 10, historicJobLogByJobPriority());

    // desc
    query
      .orderByTimestamp()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByTimestamp()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobId()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByJobId()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobDefinitionId()
      .asc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByJobDefinitionId()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobDueDate()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByJobDueDate()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobRetries()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByJobRetries()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByActivityId()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByActivityId()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByExecutionId()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByExecutionId()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByProcessInstanceId()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByProcessInstanceId()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByProcessDefinitionId()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByProcessDefinitionId()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByProcessDefinitionKey()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByProcessDefinitionKey(processEngine)));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByDeploymentId()
      .desc();
    verifyQueryWithOrdering(query, 10, inverted(historicJobLogByDeploymentId()));

    query = historyService.createHistoricJobLogQuery();

    query
      .orderByJobPriority()
      .desc();

  verifyQueryWithOrdering(query, 10, inverted(historicJobLogByJobPriority()));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQuerySortingPartiallyByOccurrence() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    String jobId = managementService.createJobQuery().singleResult().getId();

    executeAvailableJobs();
    runtimeService.setVariable(processInstanceId, "fail", false);
    managementService.executeJob(jobId);

    // asc
    HistoricJobLogQuery query = historyService
      .createHistoricJobLogQuery()
      .jobId(jobId)
      .orderPartiallyByOccurrence()
      .asc();

    verifyQueryWithOrdering(query, 5, historicJobLogPartiallyByOccurence());

    // desc
    query = historyService
        .createHistoricJobLogQuery()
        .jobId(jobId)
        .orderPartiallyByOccurrence()
        .desc();

    verifyQueryWithOrdering(query, 5, inverted(historicJobLogPartiallyByOccurence()));

    runtimeService.deleteProcessInstance(processInstanceId, null);

    // delete job /////////////////////////////////////////////////////////

    processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    jobId = managementService.createJobQuery().singleResult().getId();

    executeAvailableJobs();
    managementService.deleteJob(jobId);

    // asc
    query = historyService
      .createHistoricJobLogQuery()
      .jobId(jobId)
      .orderPartiallyByOccurrence()
      .asc();

    verifyQueryWithOrdering(query, 5, historicJobLogPartiallyByOccurence());

    // desc
    query = historyService
        .createHistoricJobLogQuery()
        .jobId(jobId)
        .orderPartiallyByOccurrence()
        .desc();

    verifyQueryWithOrdering(query, 5, inverted(historicJobLogPartiallyByOccurence()));
  }

  protected void verifyQueryResults(HistoricJobLogQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  protected void verifyQueryWithOrdering(HistoricJobLogQuery query, int countExpected, NullTolerantComparator<HistoricJobLog> expectedOrdering) {
    verifyQueryResults(query, countExpected);
    TestOrderingUtil.verifySorting(query.list(), expectedOrdering);
  }

  protected void verifySingleResultFails(HistoricJobLogQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

}
