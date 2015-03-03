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
package org.camunda.bpm.engine.test.persistence;

import java.util.List;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class ExecutionSequenceCounterTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testSequence() {
    // given

    // when
    runtimeService.startProcessInstanceByKey("process");

    // then
    ExecutionEntity instance = (ExecutionEntity) runtimeService.createExecutionQuery().singleResult();
    assertNotNull(instance);

    assertEquals(6, instance.getSequenceCounter());
  }

  @Deployment
  public void testForkSameSequenceLength() {
    // given
    ExecutionQuery query = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = query.processInstanceId(processInstanceId).list();
    assertEquals(3, executions.size());

    ExecutionEntity instance = (ExecutionEntity) executions.get(0);
    ExecutionEntity forkBranch1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(8, forkBranch2.getSequenceCounter());

    // when (2)
    String jobId = managementService.createJobQuery().activityId("theEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    instance = (ExecutionEntity) query.singleResult();

    assertEquals(10, instance.getSequenceCounter());
  }

  @Deployment
  public void testForkDifferentSequenceLength() {
    // given
    ExecutionQuery query = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then
    List<Execution> executions = query.processInstanceId(processInstanceId).list();
    assertEquals(3, executions.size());

    ExecutionEntity instance = (ExecutionEntity) executions.get(0);
    ExecutionEntity forkBranch1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(10, forkBranch2.getSequenceCounter());
  }

  @Deployment
  public void testForkReplaceBy() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(3, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity forkBranch1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(8, forkBranch2.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("theService4").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(3, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    forkBranch1 = (ExecutionEntity) executions.get(1);
    forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(12, forkBranch2.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("theEnd2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(14, instance.getSequenceCounter());

    // when (4)
    jobId = jobQuery.activityId("theService2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (4)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(16, instance.getSequenceCounter());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testForkReplaceBy.bpmn20.xml"})
  public void testForkReplaceByAnotherExecutionOrder() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(3, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity forkBranch1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(8, forkBranch2.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("theService2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(3, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    forkBranch1 = (ExecutionEntity) executions.get(1);
    forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(10, forkBranch2.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("theEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(12, instance.getSequenceCounter());

    // when (4)
    jobId = jobQuery.activityId("theService4").singleResult().getId();
    managementService.executeJob(jobId);

    // then (4)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(16, instance.getSequenceCounter());
  }

  @Deployment
  public void testForkReplaceByThreeBranches() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(4, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity forkBranch1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity forkBranch2 = (ExecutionEntity) executions.get(2);
    ExecutionEntity forkBranch3 = (ExecutionEntity) executions.get(3);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(8, forkBranch2.getSequenceCounter());
    assertEquals(8, forkBranch3.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("theService2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(4, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    forkBranch1 = (ExecutionEntity) executions.get(1);
    forkBranch2 = (ExecutionEntity) executions.get(2);
    forkBranch3 = (ExecutionEntity) executions.get(3);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(8, forkBranch2.getSequenceCounter());
    assertEquals(10, forkBranch3.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("theEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    executions = executionQuery.list();
    assertEquals(3, executions.size());

    forkBranch1 = (ExecutionEntity) executions.get(0);
    forkBranch2 = (ExecutionEntity) executions.get(1);
    instance = (ExecutionEntity)  executions.get(2);

    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(8, forkBranch2.getSequenceCounter());
    assertEquals(12, instance.getSequenceCounter());

    // when (4)
    jobId = jobQuery.activityId("theService4").singleResult().getId();
    managementService.executeJob(jobId);

    // then (4)
    executions = executionQuery.list();
    assertEquals(3, executions.size());

    forkBranch1 = (ExecutionEntity) executions.get(0);
    forkBranch2 = (ExecutionEntity) executions.get(1);
    instance = (ExecutionEntity)  executions.get(2);

    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(12, forkBranch2.getSequenceCounter());
    assertEquals(12, instance.getSequenceCounter());

    // when (5)
    jobId = jobQuery.activityId("theEnd2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (5)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(14, instance.getSequenceCounter());

    // when (6)
    jobId = jobQuery.activityId("theService7").singleResult().getId();
    managementService.executeJob(jobId);

    // then (6)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(20, instance.getSequenceCounter());
  }

  @Deployment
  public void testForkAndJoinSameSequenceLength() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(3, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity forkBranch1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(8, forkBranch2.getSequenceCounter());

    List<Job> jobs = jobQuery.list();
    assertEquals(2, jobs.size());
    String firstJobId = jobs.get(0).getId();
    String secondJobId = jobs.get(1).getId();

    // when (2)
    managementService.executeJob(firstJobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(3, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    forkBranch1 = (ExecutionEntity) executions.get(1);
    forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(9, forkBranch2.getSequenceCounter());

    // when (3)
    managementService.executeJob(secondJobId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(11, instance.getSequenceCounter());
  }

  @SuppressWarnings("unchecked")
  @Deployment
  public void testForkAndJoinDifferentSequenceLength() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(3, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity forkBranch1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(10, forkBranch2.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.executionId(forkBranch1.getId()).singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(3, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    forkBranch1 = (ExecutionEntity) executions.get(1);
    forkBranch2 = (ExecutionEntity) executions.get(2);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(9, forkBranch1.getSequenceCounter());
    assertEquals(10, forkBranch2.getSequenceCounter());

    // when (3)
    jobId = jobQuery.executionId(forkBranch2.getId()).singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.singleResult();

    List<String> order = (List<String>) runtimeService.getVariable(processInstanceId, "executionOrder");
    assertNotNull(order);
    assertEquals(2, order.size());

    if (order.get(0).equals(forkBranch1.getId())) {
      assertEquals(12, instance.getSequenceCounter());
    }
    else if (order.get(0).equals(forkBranch2.getId())) {
      assertEquals(13, instance.getSequenceCounter());
    }
    else {
      fail();
    }
  }

  @SuppressWarnings("unchecked")
  @Deployment
  public void testForkAndJoinThreeBranchesDifferentSequenceLength() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(4, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity forkBranch1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity forkBranch2 = (ExecutionEntity) executions.get(2);
    ExecutionEntity forkBranch3 = (ExecutionEntity) executions.get(3);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(8, forkBranch1.getSequenceCounter());
    assertEquals(10, forkBranch2.getSequenceCounter());
    assertEquals(12, forkBranch3.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.executionId(forkBranch1.getId()).singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(4, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    forkBranch1 = (ExecutionEntity) executions.get(1);
    forkBranch2 = (ExecutionEntity) executions.get(2);
    forkBranch3 = (ExecutionEntity) executions.get(3);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(9, forkBranch1.getSequenceCounter());
    assertEquals(10, forkBranch2.getSequenceCounter());
    assertEquals(12, forkBranch3.getSequenceCounter());

    // when (3)
    jobId = jobQuery.executionId(forkBranch2.getId()).singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    executions = executionQuery.list();
    assertEquals(4, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    forkBranch1 = (ExecutionEntity) executions.get(1);
    forkBranch2 = (ExecutionEntity) executions.get(2);
    forkBranch3 = (ExecutionEntity) executions.get(3);

    assertEquals(5, instance.getSequenceCounter());
    assertEquals(9, forkBranch1.getSequenceCounter());
    assertEquals(11, forkBranch2.getSequenceCounter());
    assertEquals(12, forkBranch3.getSequenceCounter());

    // when (4)
    jobId = jobQuery.executionId(forkBranch3.getId()).singleResult().getId();
    managementService.executeJob(jobId);

    // then (4)
    instance = (ExecutionEntity) executionQuery.singleResult();

    List<String> order = (List<String>) runtimeService.getVariable(processInstanceId, "executionOrder");
    assertNotNull(order);
    assertEquals(3, order.size());

    if (!order.contains(forkBranch3.getId())) {
      assertEquals(14, instance.getSequenceCounter());
    }
    else {
      assertEquals(15, instance.getSequenceCounter());
    }
  }

  @Deployment
  public void testSequenceInsideSubProcess() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(2, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity innerExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(9, innerExecution.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(12, instance.getSequenceCounter());
  }

  @Deployment
  public void testForkSameSequenceLengthInsideSubProcess() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(4, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity innerExecution = (ExecutionEntity) executions.get(1);
    ExecutionEntity fork1Execution = (ExecutionEntity) executions.get(2);
    ExecutionEntity fork2Execution = (ExecutionEntity) executions.get(3);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(10, innerExecution.getSequenceCounter());
    assertEquals(13, fork1Execution.getSequenceCounter());
    assertEquals(13, fork2Execution.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("innerEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(2, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    innerExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(15, innerExecution.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("innerEnd2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.singleResult();

    assertEquals(18, instance.getSequenceCounter());
  }

  @Deployment
  public void testForkDifferentSequenceLengthInsideSubProcess() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(4, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity innerExecution = (ExecutionEntity) executions.get(1);
    ExecutionEntity fork1Execution = (ExecutionEntity) executions.get(2);
    ExecutionEntity fork2Execution = (ExecutionEntity) executions.get(3);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(10, innerExecution.getSequenceCounter());
    assertEquals(13, fork1Execution.getSequenceCounter());
    assertEquals(13, fork2Execution.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("innerService3").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(4, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    innerExecution = (ExecutionEntity) executions.get(1);
    fork1Execution = (ExecutionEntity) executions.get(2);
    fork2Execution = (ExecutionEntity) executions.get(3);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(10, innerExecution.getSequenceCounter());
    assertEquals(13, fork1Execution.getSequenceCounter());
    assertEquals(15, fork2Execution.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("innerEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    executions = executionQuery.list();
    assertEquals(2, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    innerExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(17, innerExecution.getSequenceCounter());

    // when (4)
    jobId = jobQuery.activityId("innerService5").singleResult().getId();
    managementService.executeJob(jobId);

    // then (4)
    executions = executionQuery.list();
    assertEquals(2, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    innerExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(21, innerExecution.getSequenceCounter());

    // when (5)
    jobId = jobQuery.activityId("innerEnd2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (5)
    instance = (ExecutionEntity)  executionQuery.singleResult();

    assertEquals(24, instance.getSequenceCounter());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testForkDifferentSequenceLengthInsideSubProcess.bpmn20.xml"})
  public void testForkDifferentSequenceLengthInsideSubProcessAnotherExecutionOrder() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(4, executions.size());

    ExecutionEntity instance = (ExecutionEntity)  executions.get(0);
    ExecutionEntity innerExecution = (ExecutionEntity) executions.get(1);
    ExecutionEntity fork1Execution = (ExecutionEntity) executions.get(2);
    ExecutionEntity fork2Execution = (ExecutionEntity) executions.get(3);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(10, innerExecution.getSequenceCounter());
    assertEquals(13, fork1Execution.getSequenceCounter());
    assertEquals(13, fork2Execution.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.activityId("innerService5").singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(4, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    innerExecution = (ExecutionEntity) executions.get(1);
    fork1Execution = (ExecutionEntity) executions.get(2);
    fork2Execution = (ExecutionEntity) executions.get(3);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(10, innerExecution.getSequenceCounter());
    assertEquals(13, fork1Execution.getSequenceCounter());
    assertEquals(17, fork2Execution.getSequenceCounter());

    // when (3)
    jobId = jobQuery.activityId("innerEnd2").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    executions = executionQuery.list();
    assertEquals(2, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    innerExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(19, innerExecution.getSequenceCounter());

    // when (4)
    jobId = jobQuery.activityId("innerService3").singleResult().getId();
    managementService.executeJob(jobId);

    // then (4)
    executions = executionQuery.list();
    assertEquals(2, executions.size());

    instance = (ExecutionEntity)  executions.get(0);
    innerExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(21, innerExecution.getSequenceCounter());

    // when (5)
    jobId = jobQuery.activityId("innerEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (5)
    instance = (ExecutionEntity)  executionQuery.singleResult();

    assertEquals(24, instance.getSequenceCounter());
  }

  @Deployment
  public void testSequentialMultiInstance() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    TaskQuery taskQuery = taskService.createTaskQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(2, executions.size());

    ExecutionEntity instance = (ExecutionEntity) executions.get(0);
    ExecutionEntity childExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(5, childExecution.getSequenceCounter());

    // when (2)
    String taskId = taskQuery.singleResult().getId();
    taskService.complete(taskId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(2, executions.size());

    instance = (ExecutionEntity) executions.get(0);
    childExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(7, childExecution.getSequenceCounter());

    // when (3)
    taskId = taskQuery.singleResult().getId();
    taskService.complete(taskId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.singleResult();
    assertEquals(8, instance.getSequenceCounter());
  }

  @SuppressWarnings("unchecked")
  @Deployment
  public void testParallelMultiInstance() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    TaskQuery taskQuery = taskService.createTaskQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(4, executions.size());

    ExecutionEntity instance = (ExecutionEntity) executions.get(0);
    ExecutionEntity childExecution1 = (ExecutionEntity) executions.get(1);
    ExecutionEntity childExecution2 = (ExecutionEntity) executions.get(2);
    ExecutionEntity childExecution3 = (ExecutionEntity) executions.get(3);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(5, childExecution1.getSequenceCounter());
    assertEquals(5, childExecution2.getSequenceCounter());
    assertEquals(6, childExecution3.getSequenceCounter());

    // when (2)
    Task task = taskQuery.executionId(childExecution1.getId()).singleResult();
    if (task == null) {
      task = taskQuery.executionId(childExecution2.getId()).singleResult();
    }
    String firstTaskId = task.getId();
    taskService.complete(firstTaskId);

    // then (2)
    executions = executionQuery.list();
    assertEquals(4, executions.size());

    instance = (ExecutionEntity) executions.get(0);
    childExecution1 = (ExecutionEntity) executions.get(1);
    childExecution2 = (ExecutionEntity) executions.get(2);
    childExecution3 = (ExecutionEntity) executions.get(3);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(5, childExecution1.getSequenceCounter());
    assertEquals(5, childExecution2.getSequenceCounter());
    assertEquals(6, childExecution3.getSequenceCounter());

    // when (3)
    String secondTaskId = taskQuery.executionId(childExecution3.getId()).singleResult().getId();
    taskService.complete(secondTaskId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.singleResult();

    List<String> order = (List<String>) runtimeService.getVariable(processInstanceId, "executionOrder");
    assertNotNull(order);

    if (order.size() == 2) {
      assertEquals(7, instance.getSequenceCounter());
    }
    else if (order.size() == 3){
      assertEquals(8, instance.getSequenceCounter());
    }
  }

  @Deployment
  public void testLoop() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    ExecutionEntity instance = (ExecutionEntity) executionQuery.processInstanceId(processInstanceId).singleResult();
    assertNotNull(instance);

    assertEquals(6, instance.getSequenceCounter());

    // when (2)
    String jobId = jobQuery.singleResult().getId();
    managementService.executeJob(jobId);

    // then (2)
    instance = (ExecutionEntity) executionQuery.processInstanceId(processInstanceId).singleResult();
    assertNotNull(instance);

    assertEquals(12, instance.getSequenceCounter());

    // when (3)
    jobId = jobQuery.singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.processInstanceId(processInstanceId).singleResult();
    assertNotNull(instance);

    assertEquals(16, instance.getSequenceCounter());
    assertEquals("theService2", instance.getActivityId());
  }

  @Deployment
  public void testInterruptingBoundaryEvent() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(2, executions.size());

    ExecutionEntity instance = (ExecutionEntity) executions.get(0);
    ExecutionEntity childExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(5, childExecution.getSequenceCounter());

    // when (2)
    runtimeService.correlateMessage("newMessage");

    // then (2)
    instance = (ExecutionEntity) executionQuery.singleResult();
    assertNotNull(instance);

    assertEquals(8, instance.getSequenceCounter());
  }

  @Deployment
  public void testNonInterruptingBoundaryEvent() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    TaskQuery taskQuery = taskService.createTaskQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(2, executions.size());

    ExecutionEntity instance = (ExecutionEntity) executions.get(0);
    ExecutionEntity childExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(5, childExecution.getSequenceCounter());

    // when (2)
    runtimeService.correlateMessage("newMessage");

    // then (2)
    executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(3, executions.size());

    instance = (ExecutionEntity) executions.get(0);
    ExecutionEntity taskExecution = (ExecutionEntity) executions.get(1);
    ExecutionEntity boundaryExecution = (ExecutionEntity) executions.get(2);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(5, taskExecution.getSequenceCounter());
    assertEquals(6, boundaryExecution.getSequenceCounter());

    // when (3)
    String taskId = taskQuery.singleResult().getId();
    taskService.complete(taskId);
    String jobId = managementService.createJobQuery().activityId("theEnd1").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    instance = (ExecutionEntity) executionQuery.singleResult();
    assertNotNull(instance);

    assertEquals(8, instance.getSequenceCounter());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/persistence/ExecutionSequenceCounterTest.testNonInterruptingBoundaryEvent.bpmn20.xml"})
  public void testNonInterruptingBoundaryEventAnotherExecutionOrder() {
    // given
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().orderBySequenceCounter().asc();
    TaskQuery taskQuery = taskService.createTaskQuery();
    JobQuery jobQuery = managementService.createJobQuery();

    // when (1)
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // then (1)
    List<Execution> executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(2, executions.size());

    ExecutionEntity instance = (ExecutionEntity) executions.get(0);
    ExecutionEntity taskExecution = (ExecutionEntity) executions.get(1);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(5, taskExecution.getSequenceCounter());

    // when (2)
    runtimeService.correlateMessage("newMessage");

    // then (2)
    executions = executionQuery.processInstanceId(processInstanceId).list();
    assertEquals(3, executions.size());

    instance = (ExecutionEntity) executions.get(0);
    taskExecution = (ExecutionEntity) executions.get(1);
    ExecutionEntity boundaryExecution = (ExecutionEntity) executions.get(2);

    assertEquals(4, instance.getSequenceCounter());
    assertEquals(5, taskExecution.getSequenceCounter());
    assertEquals(6, boundaryExecution.getSequenceCounter());

    // when (3)
    String jobId = jobQuery.activityId("theServiceAfterMessage").singleResult().getId();
    managementService.executeJob(jobId);

    // then (3)
    executions = executionQuery.list();
    assertEquals(2, executions.size());

    taskExecution = (ExecutionEntity) executions.get(0);
    instance = (ExecutionEntity) executions.get(1);

    assertEquals(10, instance.getSequenceCounter());
    assertEquals(5, taskExecution.getSequenceCounter());

    // when (4)
    String taskId = taskQuery.singleResult().getId();
    taskService.complete(taskId);

    // then (4)
    instance = (ExecutionEntity) executionQuery.singleResult();
    assertNotNull(instance);

    assertEquals(10, instance.getSequenceCounter());
  }

}
