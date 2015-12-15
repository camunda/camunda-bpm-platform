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

  @Deployment
  public void FAILING_testForkWithThreeBranchesAndJoinOfTwoBranches() {
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

}
