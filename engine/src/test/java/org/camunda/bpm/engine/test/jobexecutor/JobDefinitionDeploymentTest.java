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
package org.camunda.bpm.engine.test.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.MessageJobDeclaration;
import org.camunda.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

/**
 * These testcases verify that job definitions are created upon deployment of the process definition.
 *
 * @author Daniel Meyer
 *
 */
public class JobDefinitionDeploymentTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testTimerStartEvent() {

    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess").singleResult();

    // then assert
    assertNotNull(jobDefinition);
    assertEquals(TimerStartEventJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("theStart", jobDefinition.getActivityId());
    assertEquals("DATE: 2036-11-14T11:12:22", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());

    // there exists a job with the correct job definition id:
    Job timerStartJob = managementService.createJobQuery().singleResult();
    assertEquals(jobDefinition.getId(), timerStartJob.getJobDefinitionId());
  }

  @Deployment
  public void testTimerBoundaryEvent() {

    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess").singleResult();

    // then assert
    assertNotNull(jobDefinition);
    assertEquals(TimerExecuteNestedActivityJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("theBoundaryEvent", jobDefinition.getActivityId());
    assertEquals("DATE: 2036-11-14T11:12:22", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());
  }

  @Deployment
  public void testMultipleTimerBoundaryEvents() {

    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess");

    // then assert
    assertEquals(2, jobDefinitionQuery.count());

    JobDefinition jobDefinition = jobDefinitionQuery.activityIdIn("theBoundaryEvent1").singleResult();
    assertNotNull(jobDefinition);
    assertEquals(TimerExecuteNestedActivityJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("theBoundaryEvent1", jobDefinition.getActivityId());
    assertEquals("DATE: 2036-11-14T11:12:22", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());

    jobDefinition = jobDefinitionQuery.activityIdIn("theBoundaryEvent2").singleResult();
    assertNotNull(jobDefinition);
    assertEquals(TimerExecuteNestedActivityJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("theBoundaryEvent2", jobDefinition.getActivityId());
    assertEquals("DURATION: PT5M", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());
  }

  @Deployment
  public void testEventBasedGateway() {

    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinitionQuery jobDefinitionQuery = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess");

    // then assert
    assertEquals(2, jobDefinitionQuery.count());

    JobDefinition jobDefinition = jobDefinitionQuery.activityIdIn("timer1").singleResult();
    assertNotNull(jobDefinition);
    assertEquals(TimerCatchIntermediateEventJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("timer1", jobDefinition.getActivityId());
    assertEquals("DURATION: PT5M", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());

    jobDefinition = jobDefinitionQuery.activityIdIn("timer2").singleResult();
    assertNotNull(jobDefinition);
    assertEquals(TimerCatchIntermediateEventJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("timer2", jobDefinition.getActivityId());
    assertEquals("DURATION: PT10M", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());
  }

  @Deployment
  public void testTimerIntermediateEvent() {

    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess").singleResult();

    // then assert
    assertNotNull(jobDefinition);
    assertEquals(TimerCatchIntermediateEventJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("timer", jobDefinition.getActivityId());
    assertEquals("DURATION: PT5M", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());
  }

  @Deployment
  public void testAsyncContinuation() {

    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess").singleResult();

    // then assert
    assertNotNull(jobDefinition);
    assertEquals(AsyncContinuationJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("theService", jobDefinition.getActivityId());
    assertEquals(MessageJobDeclaration.ASYNC_BEFORE, jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());
  }

  @Deployment
  public void testAsyncContinuationOfMultiInstance() {
    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess").singleResult();

    // then assert
    assertNotNull(jobDefinition);
    assertEquals(AsyncContinuationJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("theService" + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX, jobDefinition.getActivityId());
    assertEquals(MessageJobDeclaration.ASYNC_AFTER, jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());
  }

  @Deployment
  public void testAsyncContinuationOfActivityWrappedInMultiInstance() {
    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess").singleResult();

    // then assert
    assertNotNull(jobDefinition);
    assertEquals(AsyncContinuationJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("theService", jobDefinition.getActivityId());
    assertEquals(MessageJobDeclaration.ASYNC_AFTER, jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testAsyncContinuation.bpmn20.xml",
      "org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testMultipleProcessesWithinDeployment.bpmn20.xml"})
  public void testMultipleProcessDeployment() {
    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    List<JobDefinition> jobDefinitions = query.list();
    assertEquals(3, jobDefinitions.size());

    assertEquals(1, query.processDefinitionKey("testProcess").list().size());
    assertEquals(2, query.processDefinitionKey("anotherTestProcess").list().size());
  }

}
