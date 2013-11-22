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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
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
 * <p>These testcases verify that job definitions are created upo n deployment of the process defintion</p>
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
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess").list();

    // then assert

    // then assert
    assertEquals(2, jobDefinitions.size());
    JobDefinition jobDefinition = jobDefinitions.get(0);
    assertNotNull(jobDefinition);
    assertEquals(TimerExecuteNestedActivityJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("theBoundaryEvent1", jobDefinition.getActivityId());
    assertEquals("DATE: 2036-11-14T11:12:22", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());

    jobDefinition = jobDefinitions.get(1);
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
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery().processDefinitionKey("testProcess").list();

    // then assert

    // then assert
    assertEquals(2, jobDefinitions.size());
    JobDefinition jobDefinition = jobDefinitions.get(0);
    assertNotNull(jobDefinition);
    assertEquals(TimerCatchIntermediateEventJobHandler.TYPE, jobDefinition.getJobType());
    assertEquals("timer1", jobDefinition.getActivityId());
    assertEquals("DURATION: PT5M", jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());

    jobDefinition = jobDefinitions.get(1);
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
    assertEquals(null, jobDefinition.getJobConfiguration());
    assertEquals(processDefinition.getId(), jobDefinition.getProcessDefinitionId());
  }

  // redeployment tests ////////////////////////////////////////////////////////////

  public void testTimerStartEventRedeployment() {

    // initially there are no job definitions:
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // initial deployment
    String deploymentId = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testTimerStartEvent.bpmn20.xml")
      .deploy()
      .getId();

    // this parses the process and created the Job definitions:
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertNotNull(jobDefinition);

    // now clear the cache:
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // if we start an instance of the process, the process will be parsed again:
    runtimeService.startProcessInstanceByKey("testProcess");

    // the job has the correct definitionId set:
    Job job = managementService.createJobQuery().singleResult();
    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());

    // delete the deployment
    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testTimerBoundaryEventRedeployment() {

    // initially there are no job definitions:
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // initial deployment
    String deploymentId = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testTimerBoundaryEvent.bpmn20.xml")
      .deploy()
      .getId();

    // this parses the process and created the Job definitions:
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertNotNull(jobDefinition);

    // now clear the cache:
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // if we start an instance of the process, the process will be parsed again:
    runtimeService.startProcessInstanceByKey("testProcess");

    // no new definitions have been created:
    assertEquals(jobDefinition.getId(), managementService.createJobDefinitionQuery().singleResult().getId());

    // the job has the correct definitionId set:
    Job job = managementService.createJobQuery().singleResult();
    assertEquals(jobDefinition.getId(), job.getJobDefinitionId());

    // delete the deployment
    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testMultipleTimerBoundaryEventsRedeployment() {

    // initially there are no job definitions:
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // initial deployment
    String deploymentId = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testMultipleTimerBoundaryEvents.bpmn20.xml")
      .deploy()
      .getId();

    // this parses the process and creates the Job definitions:
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery().list();
    assertEquals(2, jobDefinitions.size());
    Set<String> definitionIds = getJobDefinitionIds(jobDefinitions);

    // now clear the cache:
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // if we start an instance of the process, the process will be parsed again:
    runtimeService.startProcessInstanceByKey("testProcess");

    // no new definitions were created
    assertEquals(2, managementService.createJobDefinitionQuery().count());

    // the job has the correct definitionId set:
    List<Job> jobs = managementService.createJobQuery().list();
    assertFalse("Both jobs were created from different job definitions",
                jobs.get(0).getJobDefinitionId().equals(jobs.get(1).getJobDefinitionId()));
    assertTrue(definitionIds.contains(jobs.get(0).getJobDefinitionId()));
    assertTrue(definitionIds.contains(jobs.get(1).getJobDefinitionId()));

    // delete the deployment
    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testEventBasedGatewayRedeployment() {

    // initially there are no job definitions:
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // initial deployment
    String deploymentId = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testEventBasedGateway.bpmn20.xml")
      .deploy()
      .getId();

    // this parses the process and creates the Job definitions:
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery().list();
    assertEquals(2, jobDefinitions.size());
    Set<String> definitionIds = getJobDefinitionIds(jobDefinitions);

    // now clear the cache:
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // if we start an instance of the process, the process will be parsed again:
    runtimeService.startProcessInstanceByKey("testProcess");

    // no new definitions were created
    assertEquals(2, managementService.createJobDefinitionQuery().count());

    // the job has the correct definitionId set:
    List<Job> jobs = managementService.createJobQuery().list();
    assertFalse("Both jobs were created from different job definitions",
                jobs.get(0).getJobDefinitionId().equals(jobs.get(1).getJobDefinitionId()));
    assertTrue(definitionIds.contains(jobs.get(0).getJobDefinitionId()));
    assertTrue(definitionIds.contains(jobs.get(1).getJobDefinitionId()));

    // delete the deployment
    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testTimerIntermediateEventRedeployment() {

    // initially there are no job definitions:
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // initial deployment
    String deploymentId = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testTimerIntermediateEvent.bpmn20.xml")
      .deploy()
      .getId();

    // this parses the process and creates the Job definitions:
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery().list();
    assertEquals(1, jobDefinitions.size());
    Set<String> definitionIds = getJobDefinitionIds(jobDefinitions);

    // now clear the cache:
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // if we start an instance of the process, the process will be parsed again:
    runtimeService.startProcessInstanceByKey("testProcess");

    // no new definitions were created
    assertEquals(1, managementService.createJobDefinitionQuery().count());

    // the job has the correct definitionId set:
    List<Job> jobs = managementService.createJobQuery().list();
    assertTrue(definitionIds.contains(jobs.get(0).getJobDefinitionId()));

    // delete the deployment
    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testAsyncContinuatioRedeployment() {

    // initially there are no job definitions:
    assertEquals(0, managementService.createJobDefinitionQuery().count());

    // initial deployment
    String deploymentId = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/jobexecutor/JobDefinitionDeploymentTest.testAsyncContinuation.bpmn20.xml")
      .deploy()
      .getId();

    // this parses the process and creates the Job definitions:
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery().list();
    assertEquals(1, jobDefinitions.size());
    Set<String> definitionIds = getJobDefinitionIds(jobDefinitions);

    // now clear the cache:
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    // if we start an instance of the process, the process will be parsed again:
    runtimeService.startProcessInstanceByKey("testProcess");

    // no new definitions were created
    assertEquals(1, managementService.createJobDefinitionQuery().count());

    // the job has the correct definitionId set:
    List<Job> jobs = managementService.createJobQuery().list();
    assertTrue(definitionIds.contains(jobs.get(0).getJobDefinitionId()));

    // delete the deployment
    repositoryService.deleteDeployment(deploymentId, true);
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

  protected Set<String> getJobDefinitionIds(List<JobDefinition> jobDefinitions) {
    Set<String> definitionIds = new HashSet<String>();
    for (JobDefinition definition : jobDefinitions) {
      definitionIds.add(definition.getId());
    }
    return definitionIds;
  }

}
