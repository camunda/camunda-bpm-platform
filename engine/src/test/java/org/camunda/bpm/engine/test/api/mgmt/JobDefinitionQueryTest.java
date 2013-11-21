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
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author roman.smirnov
 */
public class JobDefinitionQueryTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByNoCriteria() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    verifyQueryResults(query, 4);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByJobDefinitionId() {
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().jobType(TimerStartEventJobHandler.TYPE).singleResult();

    JobDefinitionQuery query = managementService.createJobDefinitionQuery().jobDefinitionId(jobDefinition.getId());

    verifyQueryResults(query, 1);

    assertEquals(jobDefinition.getId(), query.singleResult().getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByInvalidJobDefinitionId() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().jobDefinitionId("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobDefinitionQuery().jobDefinitionId(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByActivityId() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().activityIdIn("ServiceTask_1");
    verifyQueryResults(query, 1);

    query = managementService.createJobDefinitionQuery().activityIdIn("ServiceTask_1", "BoundaryEvent_1");
    verifyQueryResults(query, 2);

    query = managementService.createJobDefinitionQuery().activityIdIn("ServiceTask_1", "BoundaryEvent_1", "StartEvent_1");
    verifyQueryResults(query, 3);

    query = managementService.createJobDefinitionQuery().activityIdIn("ServiceTask_1", "BoundaryEvent_1", "StartEvent_1", "IntermediateCatchEvent_1");
    verifyQueryResults(query, 4);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByInvalidActivityId() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().activityIdIn("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobDefinitionQuery().activityIdIn(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}

    try {
      managementService.createJobDefinitionQuery().activityIdIn((String)null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByProcessDefinitionId() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    JobDefinitionQuery query = managementService.createJobDefinitionQuery().processDefinitionId(processDefinition.getId());
    verifyQueryResults(query, 4);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByInvalidDefinitionId() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().processDefinitionId("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobDefinitionQuery().processDefinitionId(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByProcessDefinitionKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    JobDefinitionQuery query = managementService.createJobDefinitionQuery().processDefinitionKey(processDefinition.getKey());
    verifyQueryResults(query, 4);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByInvalidDefinitionKey() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().processDefinitionKey("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobDefinitionQuery().processDefinitionKey(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByJobType() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().jobType(AsyncContinuationJobHandler.TYPE);
    verifyQueryResults(query, 1);

    query = managementService.createJobDefinitionQuery().jobType(TimerStartEventJobHandler.TYPE);
    verifyQueryResults(query, 1);

    query = managementService.createJobDefinitionQuery().jobType(TimerCatchIntermediateEventJobHandler.TYPE);
    verifyQueryResults(query, 1);

    query = managementService.createJobDefinitionQuery().jobType(TimerExecuteNestedActivityJobHandler.TYPE);
    verifyQueryResults(query, 1);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByInvalidJobType() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().jobType("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobDefinitionQuery().jobType(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByInvalidJobConfiguration() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().jobConfiguration("invalid");
    verifyQueryResults(query, 0);

    try {
      managementService.createJobDefinitionQuery().jobConfiguration(null);
      fail("A ProcessEngineExcpetion was expected.");
    } catch (ProcessEngineException e) {}
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryByActive() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().active();
    verifyQueryResults(query, 4);

    // suspend first one
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().jobType(AsyncContinuationJobHandler.TYPE).singleResult();
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // only three active job definitions left
    verifyQueryResults(query, 3);

    // Suspend second one
    jobDefinition = managementService.createJobDefinitionQuery().jobType(TimerStartEventJobHandler.TYPE).singleResult();
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // only two active job definitions left
    verifyQueryResults(query, 2);

    // suspend third one
    jobDefinition = managementService.createJobDefinitionQuery().jobType(TimerCatchIntermediateEventJobHandler.TYPE).singleResult();
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // only two active job definitions left
    verifyQueryResults(query, 1);

    // suspend fourth one
    jobDefinition = managementService.createJobDefinitionQuery().jobType(TimerExecuteNestedActivityJobHandler.TYPE).singleResult();
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // no one is active
    verifyQueryResults(query, 0);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryBySuspended() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery().suspended();
    verifyQueryResults(query, 0);

    // suspend first one
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().jobType(AsyncContinuationJobHandler.TYPE).singleResult();
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // only one is suspended
    verifyQueryResults(query, 1);

    // Suspend second one
    jobDefinition = managementService.createJobDefinitionQuery().jobType(TimerStartEventJobHandler.TYPE).singleResult();
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // only two are suspended
    verifyQueryResults(query, 2);

    // suspend third one
    jobDefinition = managementService.createJobDefinitionQuery().jobType(TimerCatchIntermediateEventJobHandler.TYPE).singleResult();
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // only three are suspended
    verifyQueryResults(query, 3);

    // suspend fourth one
    jobDefinition = managementService.createJobDefinitionQuery().jobType(TimerExecuteNestedActivityJobHandler.TYPE).singleResult();
    managementService.suspendJobDefinitionById(jobDefinition.getId());

    // all are suspended
    verifyQueryResults(query, 4);
  }

  // Pagination //////////////////////////////////////////////////////////

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQueryPaging() {
    assertEquals(4, managementService.createJobDefinitionQuery().listPage(0, 4).size());
    assertEquals(1, managementService.createJobDefinitionQuery().listPage(2, 1).size());
    assertEquals(2, managementService.createJobDefinitionQuery().listPage(1, 2).size());
    assertEquals(3, managementService.createJobDefinitionQuery().listPage(1, 4).size());
  }

  // Sorting /////////////////////////////////////////////////////////////

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/mgmt/JobDefinitionQueryTest.testBase.bpmn"})
  public void testQuerySorting() {
    // asc
    assertEquals(4, managementService.createJobDefinitionQuery().orderByActivityId().asc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByJobConfiguration().asc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByJobDefinitionId().asc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByJobType().asc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByProcessDefinitionKey().asc().list().size());

    // desc
    assertEquals(4, managementService.createJobDefinitionQuery().orderByActivityId().desc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByJobConfiguration().desc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByJobDefinitionId().desc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByJobType().desc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(4, managementService.createJobDefinitionQuery().orderByProcessDefinitionKey().desc().list().size());

  }

  public void testQueryInvalidSortingUsage() {
    try {
      managementService.createJobDefinitionQuery().orderByJobDefinitionId().list();
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("call asc() or desc() after using orderByXX()", e.getMessage());
    }

    try {
      managementService.createJobQuery().asc();
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("You should call any of the orderBy methods first before specifying a direction", e.getMessage());
    }
  }

  // Test Helpers ////////////////////////////////////////////////////////

  private void verifyQueryResults(JobDefinitionQuery query, int countExpected) {
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

  private void verifySingleResultFails(JobDefinitionQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

}
