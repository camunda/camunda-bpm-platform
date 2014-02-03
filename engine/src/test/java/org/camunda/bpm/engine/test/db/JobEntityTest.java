package org.camunda.bpm.engine.test.db;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.Deployment;

import java.util.List;

/**
 *
 * @author Clint Manning
 */
public class JobEntityTest extends PluggableProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/test/db/processWithGatewayAndTwoEndEvents.bpmn20.xml"})
  public void testGatewayWithTwoEndEventsLastJobReAssignedToParentExe() {
    String processKey = repositoryService.createProcessDefinitionQuery().singleResult().getKey();
    String processInstanceId = runtimeService.startProcessInstanceByKey(processKey).getId();

    List<Job> jobList = managementService.createJobQuery().processInstanceId(processInstanceId).list();

    // There should be two jobs
    assertNotNull(jobList);
    assertEquals(2, jobList.size());

    managementService.executeJob(jobList.get(0).getId());

    // There should be only one job left
    jobList = managementService.createJobQuery().list();
    assertEquals(1, jobList.size());

    // There should only be 1 execution left - the root execution
    assertEquals(1, runtimeService.createExecutionQuery().list().size());

    // root execution should be attached to the last job
    assertEquals(processInstanceId, jobList.get(0).getExecutionId());

    managementService.executeJob(jobList.get(0).getId());

    // There should be no more jobs
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/db/processGatewayAndTwoEndEventsPlusTimer.bpmn20.xml"})
  public void testGatewayWithTwoEndEventsLastTimerReAssignedToParentExe() {
    String processKey = repositoryService.createProcessDefinitionQuery().singleResult().getKey();
    String processInstanceId = runtimeService.startProcessInstanceByKey(processKey).getId();

    List<Job> jobList = managementService.createJobQuery().processInstanceId(processInstanceId).list();

    // There should be two jobs
    assertNotNull(jobList);
    assertEquals(2, jobList.size());

    // execute timer first
    String timerId = managementService.createJobQuery().timers().singleResult().getId();
    managementService.executeJob(timerId);

    // There should be only one job left
    jobList = managementService.createJobQuery().list();
    assertEquals(1, jobList.size());

    // There should only be 1 execution left - the root execution
    assertEquals(1, runtimeService.createExecutionQuery().list().size());

    // root execution should be attached to the last job
    assertEquals(processInstanceId, jobList.get(0).getExecutionId());

    // execute service task
    managementService.executeJob(jobList.get(0).getId());

    // There should be no more jobs
    jobList = managementService.createJobQuery().list();
    assertEquals(0, jobList.size());
  }
}
