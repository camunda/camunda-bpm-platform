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
package org.camunda.bpm.qa.upgrade;

import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import java.util.List;

import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class TestActivityInstanceUpgrade extends AbstractDbUpgradeTestCase {

  @Test
  public void testSingleTaskProcess() {
    String processDefinitionKey = "TestFixture70.singleTaskProcess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
         describeActivityInstanceTree()
           .activity("waitHere")
         .done());

    // assert that the process instance can be completed:
    Task task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();

    taskService.complete(task.getId());
  }

  @Test
  public void testNestedSingleTaskProcess() {
    String processDefinitionKey = "TestFixture70.nestedSingleTaskProcess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("waitHere")
          .endScope()
        .done());

    // assert that the process instance can be completed:
    Task task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();

    taskService.complete(task.getId());
  }

  @Test
  public void testConcurrentTaskProcess() {
    String processDefinitionKey = "TestFixture70.concurrentTaskProcess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .activity("task1")
          .activity("task2")
        .done());

    // complete first task
    Task firstTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("task1")
      .singleResult();
    taskService.complete(firstTask.getId());

    // task removed from tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .activity("task2")
        .done());

    // complete second task
    taskService.complete(taskService.createTaskQuery().processDefinitionKey(processDefinitionKey).singleResult().getId());
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedConcurrentTaskProcess() {
    String processDefinitionKey = "TestFixture70.nestedConcurrentTaskProcess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("task1")
            .activity("task2")
          .endScope()
        .done());

    // complete first task
    Task firstTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("task1")
      .singleResult();
    taskService.complete(firstTask.getId());

    // task removed from tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("task2")
          .endScope()
        .done());

    // complete second task
    taskService.complete(taskService.createTaskQuery().processDefinitionKey(processDefinitionKey).singleResult().getId());
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testJoinOneExecutionProcess() {
    String processDefinitionKey = "TestFixture70.joinOneExecutionProcess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .activity("task1")
          .activity("join")
        .done());

    // complete first task
    Task firstTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(firstTask.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedJoinOneExecutionProcess() {
    String processDefinitionKey = "TestFixture70.nestedJoinOneExecutionProcess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("task1")
            .activity("join")
          .endScope()
        .done());

    // complete first task
    Task firstTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(firstTask.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testJoinTwoExecutionsProcess() {
    String processDefinitionKey = "TestFixture70.joinTwoExecutionsProcess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .activity("task1")
          .activity("join")
          .activity("join")
        .done());

    // complete first task
    Task firstTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(firstTask.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedJoinTwoExecutionsProcess() {
    String processDefinitionKey = "TestFixture70.nestedJoinTwoExecutionsProcess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("task1")
            .activity("join")
            .activity("join")
          .endScope()
        .done());

    // complete first task
    Task firstTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(firstTask.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testSingleEmbeddedSubprocess() {
    String processDefinitionKey = "TestFixture70.singleEmbeddedSubprocess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
          .endScope()
        .done());

    // assert that the process instance can be completed:
    List<Task> tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete first task
    taskService.complete(tasks.get(0).getId());

    // process ended
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }


  @Test
  public void testNestedSingleEmbeddedSubprocess() {
    String processDefinitionKey = "TestFixture70.nestedSingleEmbeddedSubprocess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside1")
            .endScope()
          .endScope()
        .done());

    // assert that the process instance can be completed:
    List<Task> tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete first task
    taskService.complete(tasks.get(0).getId());

    // process ended
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testConcurrentEmbeddedSubprocess() {
    String processDefinitionKey = "TestFixture70.concurrentEmbeddedSubprocess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
          .endScope()
          .beginScope("scope2")
            .activity("waitInside2")
          .endScope()
        .done());

    // complete first task
    Task firstTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside1")
      .singleResult();
    taskService.complete(firstTask.getId());

    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope2")
            .activity("waitInside2")
          .endScope()
        .done());

    // complete second task
    Task secondTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(secondTask.getId());

    // process ended
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedConcurrentEmbeddedSubprocess() {
    String processDefinitionKey = "TestFixture70.nestedConcurrentEmbeddedSubprocess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside1")
            .endScope()
            .beginScope("scope2")
              .activity("waitInside2")
            .endScope()
          .endScope()
        .done());

    // complete first task
    Task firstTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside1")
      .singleResult();
    taskService.complete(firstTask.getId());

    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope2")
              .activity("waitInside2")
            .endScope()
          .endScope()
        .done());

    // complete second task
    Task secondTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(secondTask.getId());

    // process ended
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testMultiInstanceSequentialTask() {
    String processDefinitionKey = "TestFixture70.multiInstanceSequentialTask";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .activity("waitHere")
        .done());

    // complete first task
    Task task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(task.getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .activity("waitHere")
        .done());

    // complete second task
    task = taskService
        .createTaskQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult();
    taskService.complete(task.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedMultiInstanceSequentialTask() {
    String processDefinitionKey = "TestFixture70.nestedMultiInstanceSequentialTask";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("waitHere")
          .endScope()
        .done());

    // complete first task
    Task task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(task.getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("waitHere")
          .endScope()
        .done());

    // complete second task
    task = taskService
        .createTaskQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult();
    taskService.complete(task.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testMultiInstanceSequentialSubprocess() {
    String processDefinitionKey = "TestFixture70.multiInstanceSequentialSubprocess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
          .endScope()
        .done());

    // complete first task
    Task task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(task.getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
          .endScope()
        .done());

    // complete second task
    task = taskService
        .createTaskQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult();
    taskService.complete(task.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedMultiInstanceSequentialSubprocess() {
    String processDefinitionKey = "TestFixture70.nestedMultiInstanceSequentialSubprocess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside1")
            .endScope()
          .endScope()
        .done());

    // complete first task
    Task task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    taskService.complete(task.getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
      describeActivityInstanceTree()
        .beginScope("outerProcess")
          .beginScope("scope1")
            .activity("waitInside1")
          .endScope()
        .endScope()
      .done());

    // complete second task
    task = taskService
        .createTaskQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult();
    taskService.complete(task.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testMultiInstanceSequentialSubprocessConcurrent() {
    String processDefinitionKey = "TestFixture70.multiInstanceSequentialSubprocessConcurrent";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
            .activity("waitInside2")
          .endScope()
        .done());

    // complete first task
    Task task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside1")
      .singleResult();
    taskService.complete(task.getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside2")
          .endScope()
        .done());

    // complete second task
    task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside2")
      .singleResult();
    taskService.complete(task.getId());

    // second instance created:
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
            .activity("waitInside2")
          .endScope()
        .done());

    // complete first task
    task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside1")
      .singleResult();
    taskService.complete(task.getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside2")
          .endScope()
        .done());

    // complete second task
    task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside2")
      .singleResult();
    taskService.complete(task.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  /** this is still failing */
  @Test
  @Ignore
  public void testNestedMultiInstanceSequentialSubprocessConcurrent() {
    String processDefinitionKey = "TestFixture70.nestedMultiInstanceSequentialSubprocessConcurrent";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    // validate tree
    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside1")
              .activity("waitInside2")
            .endScope()
          .endScope()
        .done());

    // complete first task
    Task task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside1")
      .singleResult();
    taskService.complete(task.getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside2")
            .endScope()
          .endScope()
        .done());

    // complete second task
    task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside2")
      .singleResult();
    taskService.complete(task.getId());

    // second instance created
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside1")
              .activity("waitInside2")
            .endScope()
          .endScope()
        .done());

    // complete first task
    task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside1")
      .singleResult();
    taskService.complete(task.getId());

    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside2")
            .endScope()
          .endScope()
        .done());

    // complete second task
    task = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside2")
      .singleResult();
    taskService.complete(task.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testMultiInstanceParallelTask() {
    String processDefinitionKey = "TestFixture70.multiInstanceParallelTask";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .activity("waitHere")
          .activity("waitHere")
        .done());

    // assert that the process instance can be completed:
    List<Task> tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete first task
    taskService.complete(tasks.get(0).getId());

    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .activity("waitHere")
        .done());

    // assert that the process instance can be completed:
    tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();

    // complete second task
    taskService.complete(tasks.get(0).getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedMultiInstanceParallelTask() {
    String processDefinitionKey = "TestFixture70.nestedMultiInstanceParallelTask";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("waitHere")
            .activity("waitHere")
          .endScope()
        .done());

    // assert that the process instance can be completed:
    List<Task> tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete first task
    taskService.complete(tasks.get(0).getId());

    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .activity("waitHere")
          .endScope()
        .done());

    // assert that the process instance can be completed:
    tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete second task
    taskService.complete(tasks.get(0).getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testMultiInstanceParallelSubprocess() {
    String processDefinitionKey = "TestFixture70.multiInstanceParallelSubprocess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
          .endScope()
          .beginScope("scope1")
            .activity("waitInside1")
          .endScope()
        .done());

    List<Task> tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete first task
    taskService.complete(tasks.get(0).getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
          .endScope()
          .activity("scope1")
        .done());

    tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete next task
    taskService.complete(tasks.get(0).getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedMultiInstanceParallelSubprocess() {
    String processDefinitionKey = "TestFixture70.nestedMultiInstanceParallelSubprocess";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside1")
            .endScope()
            .beginScope("scope1")
              .activity("waitInside1")
            .endScope()
          .endScope()
        .done());

    List<Task> tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete first task
    taskService.complete(tasks.get(0).getId());

    // validate tree
    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside1")
            .endScope()
            .activity("scope1")
          .endScope()
        .done());

    tasks = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .list();
    // complete next task
    taskService.complete(tasks.get(0).getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testMultiInstanceParallelSubprocessConcurrent() {

    String processDefinitionKey = "TestFixture70.multiInstanceParallelSubprocessConcurrent";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside1")
            .activity("waitInside2")
          .endScope()
          .beginScope("scope1")
            .activity("waitInside1")
            .activity("waitInside2")
          .endScope()
        .done());

    List<Task> waitInside1 = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside1")
      .listPage(0, 2);
    taskService.complete(waitInside1.get(0).getId());
    taskService.complete(waitInside1.get(1).getId());

    List<Task> waitInside2 = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside2")
      .listPage(0, 1);

    // this completes the first subprocess instance
    taskService.complete(waitInside2.get(0).getId());

    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("scope1")
            .activity("waitInside2")
          .endScope()
          .activity("scope1")
        .done());

    // assert that the process instance can be completed:
    Task lastTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    // complete first task
    taskService.complete(lastTask.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

  @Test
  public void testNestedMultiInstanceParallelSubprocessConcurrent() {

    String processDefinitionKey = "TestFixture70.nestedMultiInstanceParallelSubprocessConcurrent";

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    Assert.assertNotNull(processInstance);

    ActivityInstance actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside1")
              .activity("waitInside2")
            .endScope()
            .beginScope("scope1")
              .activity("waitInside1")
              .activity("waitInside2")
            .endScope()
          .endScope()
        .done());

    List<Task> waitInside1 = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside1")
      .listPage(0, 2);
    taskService.complete(waitInside1.get(0).getId());
    taskService.complete(waitInside1.get(1).getId());

    List<Task> waitInside2 = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .taskDefinitionKey("waitInside2")
      .listPage(0, 1);

    // this completes the first subprocess instance
    taskService.complete(waitInside2.get(0).getId());

    actualTree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(actualTree).hasStructure(
        describeActivityInstanceTree()
          .beginScope("outerProcess")
            .beginScope("scope1")
              .activity("waitInside2")
            .endScope()
            .activity("scope1")
          .endScope()
        .done());

    // assert that the process instance can be completed:
    Task lastTask = taskService
      .createTaskQuery()
      .processDefinitionKey(processDefinitionKey)
      .singleResult();
    // complete first task
    taskService.complete(lastTask.getId());

    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
  }

}
