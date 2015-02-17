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
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByJobDueDate;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByJobId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByJobRetries;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByProcessDefinitionId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByProcessDefinitionKey;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByProcessInstanceId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicJobLogByTimestamp;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricJobLogQuery;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil;
import org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.NullTolerantComparator;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogQueryTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQuery() {
    runtimeService.startProcessInstanceByKey("process");
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery();

    verifyQueryResults(query, 1);
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
  public void testQueryByHandlerType() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobHandlerType(AsyncContinuationJobHandler.TYPE);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidHandlerType() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().jobHandlerType("invalid");

    verifyQueryResults(query, 0);

    try {
      query.jobHandlerType(null);
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

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByExceptionMessage() {
    runtimeService.startProcessInstanceByKey("process");
    String jobId = managementService.createJobQuery().singleResult().getId();
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().exceptionMessage(FailingDelegate.EXCEPTION_MESSAGE);

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidExceptionMessage() {
    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().exceptionMessage("invalid");

    verifyQueryResults(query, 0);

    try {
      query.exceptionMessage(null);
      fail();
    } catch (Exception e) {
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testIntermediateTimerEvent.bpmn20.xml"})
  public void testQueryByTimers() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().timers();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByMessages() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().messages();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByCreated() {
    runtimeService.startProcessInstanceByKey("process");

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().created();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByFailed() {
    runtimeService.startProcessInstanceByKey("process");
    String jobId = managementService.createJobQuery().singleResult().getId();
    try {
      managementService.executeJob(jobId);
      fail();
    } catch (Exception e) {
      // expected
    }

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().failed();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryBySucceeded() {
    runtimeService.startProcessInstanceByKey("process", Variables.createVariables().putValue("fail", false));
    String jobId = managementService.createJobQuery().singleResult().getId();
    managementService.executeJob(jobId);

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().successful();

    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryByDeleted() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();
    runtimeService.deleteProcessInstance(processInstanceId, null);

    HistoricJobLogQuery query = historyService.createHistoricJobLogQuery().deleted();

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
