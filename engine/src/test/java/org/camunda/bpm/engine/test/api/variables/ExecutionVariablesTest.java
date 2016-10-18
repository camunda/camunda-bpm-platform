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
package org.camunda.bpm.engine.test.api.variables;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class ExecutionVariablesTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testTreeCompactionWithLocalVariableOnConcurrentExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Execution innerTaskExecution = runtimeService
        .createExecutionQuery()
        .activityId("innerTask")
        .singleResult();

    Execution subProcessConcurrentExecution = runtimeService
        .createExecutionQuery()
        .executionId(((ExecutionEntity) innerTaskExecution).getParentId())
        .singleResult();

    Task task = taskService
        .createTaskQuery()
        .taskDefinitionKey("task")
        .singleResult();

    // when
    runtimeService.setVariableLocal(subProcessConcurrentExecution.getId(), "foo", "bar");
    // and completing the concurrent task, thereby pruning the sub process concurrent execution
    taskService.complete(task.getId());

    // then the variable still exists
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variable);
    assertEquals("foo", variable.getName());
    assertEquals(processInstance.getId(), variable.getExecutionId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionWithLocalVariableOnConcurrentExecution.bpmn20.xml")
  public void testStableVariableInstanceIdsOnCompaction() {
    runtimeService.startProcessInstanceByKey("process");

    Execution innerTaskExecution = runtimeService
        .createExecutionQuery()
        .activityId("innerTask")
        .singleResult();

    Execution subProcessConcurrentExecution = runtimeService
        .createExecutionQuery()
        .executionId(((ExecutionEntity) innerTaskExecution).getParentId())
        .singleResult();

    Task task = taskService
        .createTaskQuery()
        .taskDefinitionKey("task")
        .singleResult();

    // when
    runtimeService.setVariableLocal(subProcessConcurrentExecution.getId(), "foo", "bar");
    VariableInstance variableBeforeCompaction = runtimeService.createVariableInstanceQuery().singleResult();

    // and completing the concurrent task, thereby pruning the sub process concurrent execution
    taskService.complete(task.getId());

    // then the variable still exists
    VariableInstance variableAfterCompaction = runtimeService.createVariableInstanceQuery().singleResult();
    assertEquals(variableBeforeCompaction.getId(), variableAfterCompaction.getId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionForkParallelGateway.bpmn20.xml")
  public void testStableVariableInstanceIdsOnCompactionAndExpansion() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    Task task2 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task1Execution.getId(), "foo", "bar");
    VariableInstance variableBeforeCompaction = runtimeService.createVariableInstanceQuery().singleResult();

    // compacting the tree
    taskService.complete(task2.getId());

    // expanding the tree
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute();

    // then the variable still exists
    VariableInstance variableAfterCompaction = runtimeService.createVariableInstanceQuery().singleResult();
    assertEquals(variableBeforeCompaction.getId(), variableAfterCompaction.getId());
  }

  @Deployment
  public void testTreeCompactionForkParallelGateway() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task1 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    // and completing the other task, thereby pruning the concurrent execution
    taskService.complete(task1.getId());

    // then the variable still exists
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variable);
    assertEquals("foo", variable.getName());
    assertEquals(processInstance.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testTreeCompactionNestedForkParallelGateway() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    Task task1 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();
    String subProcessScopeExecutionId = ((ExecutionEntity) task2Execution).getParentId();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    // and completing the other task, thereby pruning the concurrent execution
    taskService.complete(task1.getId());

    // then the variable still exists on the subprocess scope execution
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variable);
    assertEquals("foo", variable.getName());
    assertEquals(subProcessScopeExecutionId, variable.getExecutionId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionForkParallelGateway.bpmn20.xml")
  public void testTreeCompactionWithVariablesOnScopeAndConcurrentExecution() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task1 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariable(processInstance.getId(), "foo", "baz");
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    // and completing the other task, thereby pruning the concurrent execution
    taskService.complete(task1.getId());

    // then something happens
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variable);
    assertEquals("foo", variable.getName());
    assertEquals(processInstance.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testForkWithThreeBranchesAndJoinOfTwoBranchesParallelGateway() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    taskService.complete(taskService.createTaskQuery().taskDefinitionKey("task1").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskDefinitionKey("task2").singleResult().getId());

    // then
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());
  }

  @Deployment
  public void testForkWithThreeBranchesAndJoinOfTwoBranchesInclusiveGateway() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    taskService.complete(taskService.createTaskQuery().taskDefinitionKey("task1").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskDefinitionKey("task2").singleResult().getId());

    // then
    assertEquals(0, runtimeService.createVariableInstanceQuery().count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionForkParallelGateway.bpmn20.xml")
  public void testTreeCompactionAndExpansionWithConcurrentLocalVariables() {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Execution task1Execution = runtimeService.createExecutionQuery().activityId("task1").singleResult();
    Task task2 = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();

    runtimeService.setVariableLocal(task1Execution.getId(), "var", "value");

    // when compacting the tree
    taskService.complete(task2.getId());

    // and expanding again
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute();

    // then the variable is again assigned to task1's concurrent execution
    Task task1 = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult();
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();

    assertEquals(task1.getExecutionId(), variable.getExecutionId());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionForkParallelGateway.bpmn20.xml")
  public void testTreeCompactionAndExpansionWithScopeExecutionVariables() {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task2 = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();

    runtimeService.setVariableLocal(processInstance.getId(), "var", "value");

    // when compacting the tree
    taskService.complete(task2.getId());

    // and expanding again
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute();

    // then the variable is still assigned to the scope execution execution
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();

    assertEquals(processInstance.getId(), variable.getExecutionId());
  }

}
