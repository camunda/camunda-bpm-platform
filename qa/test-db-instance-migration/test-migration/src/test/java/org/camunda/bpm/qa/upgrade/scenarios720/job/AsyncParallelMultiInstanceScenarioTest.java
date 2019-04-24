/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.upgrade.scenarios720.job;

import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import java.util.List;

import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.UpgradeTestRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("AsyncParallelMultiInstanceScenario")
@Origin("7.2.0")
public class AsyncParallelMultiInstanceScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("initAsyncBeforeSubprocess.1")
  public void testInitAsyncBeforeSubprocessCompletion() {
    // given
    Job asyncJob = rule.jobQuery().singleResult();

    // when
    rule.getManagementService().executeJob(asyncJob.getId());

    // then the process can be completed successfully
    List<Task> subProcessTasks = rule.taskQuery().list();
    Assert.assertEquals(3, subProcessTasks.size());

    for (Task subProcessTask : subProcessTasks) {
      rule.getTaskService().complete(subProcessTask.getId());
    }

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initAsyncBeforeSubprocess.2")
  public void testInitAsyncBeforeSubprocessActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          // this is not the multi-instance body because the execution
          // references the inner activity
          .transition("miSubProcess")
        .done());
  }

  @Test
  @ScenarioUnderTest("initAsyncBeforeSubprocess.3")
  public void testInitAsyncBeforeSubprocessDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  /**
   * Note: this test is not really isolated since the job
   * definition is migrated when the process definition is accessed the first time.
   * This might happen already before this test case is executed.
   */
  @Test
  @ScenarioUnderTest("initAsyncBeforeSubprocess.4")
  public void testInitAsyncBeforeSubprocessJobDefinition() {
    // when the process is redeployed into the cache (instantiation should trigger that)
    rule.getRuntimeService().startProcessInstanceByKey("AsyncBeforeParallelMultiInstanceSubprocess");

    // then the old job definition referencing "miSubProcess" has been migrated
    JobDefinition asyncJobDefinition = rule.jobDefinitionQuery().singleResult();
    Assert.assertEquals("miSubProcess#multiInstanceBody", asyncJobDefinition.getActivityId());
  }

  @Test
  @ScenarioUnderTest("initAsyncBeforeTask.1")
  public void testInitAsyncBeforeTaskCompletion() {
    // given
    Job asyncJob = rule.jobQuery().singleResult();

    // when
    rule.getManagementService().executeJob(asyncJob.getId());

    // then the process can be completed successfully
    List<Task> subProcessTasks = rule.taskQuery().list();
    Assert.assertEquals(3, subProcessTasks.size());

    for (Task subProcessTask : subProcessTasks) {
      rule.getTaskService().complete(subProcessTask.getId());
    }

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initAsyncBeforeTask.2")
  public void testInitAsyncBeforeTaskActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());

    // then
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
        describeActivityInstanceTree(instance.getProcessDefinitionId())
          // this is not the multi-instance body because the execution
          // references the inner activity
          .transition("miTask")
        .done());
  }

  @Test
  @ScenarioUnderTest("initAsyncBeforeTask.3")
  public void testInitAsyncBeforeTaskDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  /**
   * Note: this test is not really isolated since the job
   * definition is migrated when the process definition is accessed the first time.
   * This might happen already before this test case is executed.
   */
  @Test
  @ScenarioUnderTest("initAsyncBeforeTask.4")
  public void testInitAsyncBeforeTaskJobDefinition() {
    // when the process is redeployed into the cache (instantiation should trigger that)
    rule.getRuntimeService().startProcessInstanceByKey("AsyncBeforeParallelMultiInstanceTask");

    // then the old job definition referencing "miSubProcess" has been migrated
    JobDefinition asyncJobDefinition = rule.jobDefinitionQuery().singleResult();
    Assert.assertEquals("miTask#multiInstanceBody", asyncJobDefinition.getActivityId());
  }

}
