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
package org.camunda.bpm.engine.test.task;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class TaskListenerTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testCreateListenerByClass.cmmn"})
  public void testCreateListenerByClass() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(3, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testCreateListenerByExpression.cmmn"})
  public void testCreateListenerByExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MyTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testCreateListenerByDelegateExpression.cmmn"})
  public void testCreateListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MySpecialTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testCreateListenerByScript.cmmn"})
  public void testCreateListenerByScript() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(2, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testCompleteListenerByClass.cmmn"})
  public void testCompleteListenerByClass() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(3, query.count());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testCompleteListenerByExpression.cmmn"})
  public void testCompleteListenerByExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MyTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testCompleteListenerByDelegateExpression.cmmn"})
  public void testCompleteListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MySpecialTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testCompleteListenerByScript.cmmn"})
  public void testCompleteListenerByScript() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(2, query.count());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testDeleteListenerByClass.cmmn"})
  public void testDeleteListenerByClass() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(3, query.count());

    assertTrue((Boolean) query.variableName("delete").singleResult().getValue());
    assertEquals(1, query.variableName("deleteEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testDeleteListenerByExpression.cmmn"})
  public void testDeleteListenerByExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MyTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("delete").singleResult().getValue());
    assertEquals(1, query.variableName("deleteEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testDeleteListenerByDelegateExpression.cmmn"})
  public void testDeleteListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MySpecialTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("delete").singleResult().getValue());
    assertEquals(1, query.variableName("deleteEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testDeleteListenerByScript.cmmn"})
  public void testDeleteListenerByScript() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(2, query.count());

    assertTrue((Boolean) query.variableName("delete").singleResult().getValue());
    assertEquals(1, query.variableName("deleteEventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testDeleteListenerByCaseInstanceDeletion.cmmn"})
  public void testDeleteListenerByCaseInstanceDeletion() {
    TaskDeleteListener.clear();

    // given
    final String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // when
    processEngineConfiguration
      .getCommandExecutorTxRequired()
      .execute(new Command<Void>() {

        @Override
        public Void execute(CommandContext commandContext) {
          commandContext
            .getCaseExecutionManager()
            .deleteCaseInstance(caseInstanceId, null);
          return null;
        }

      });

    // then
    assertEquals(1, TaskDeleteListener.eventCounter);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAssignmentListenerByClass.cmmn"})
  public void testAssignmentListenerByClass() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    // when
    taskService.setAssignee(taskId, "jonny");

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(3, query.count());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAssignmentListenerByExpression.cmmn"})
  public void testAssignmentListenerByExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MyTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    // when
    taskService.setAssignee(taskId, "jonny");

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAssignmentListenerByDelegateExpression.cmmn"})
  public void testAssignmentListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MySpecialTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    // when
    taskService.setAssignee(taskId, "jonny");

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAssignmentListenerByScript.cmmn"})
  public void testAssignmentListenerByScript() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    // when
    taskService.setAssignee(taskId, "jonny");

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(2, query.count());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAssignmentListenerByInitialInstantiation.cmmn"})
  public void testAssignmentListenerByInitialInstantiation() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(3, query.count());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());
    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAllListenerByClass.cmmn"})
  public void testAllListenerByClassExcludingDeletion() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(7, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAllListenerByClass.cmmn"})
  public void testAllListenerByClassExcludingCompletion() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(7, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("delete").singleResult().getValue());
    assertEquals(1, query.variableName("deleteEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAllListenerByExpression.cmmn"})
  public void testAllListenerByExpressionExcludingDeletion() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MyTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(8, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAllListenerByExpression.cmmn"})
  public void testAllListenerByExpressionExcludingCompletion() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MyTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(8, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("delete").singleResult().getValue());
    assertEquals(1, query.variableName("deleteEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAllListenerByDelegateExpression.cmmn"})
  public void testAllListenerByDelegateExpressionExcludingDeletion() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MySpecialTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(8, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAllListenerByDelegateExpression.cmmn"})
  public void testAllListenerByDelegateExpressionExcludingCompletion() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new MySpecialTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(8, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("delete").singleResult().getValue());
    assertEquals(1, query.variableName("deleteEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAllListenerByScript.cmmn"})
  public void testAllListenerByScriptExcludingDeletion() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(7, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testAllListenerByScript.cmmn"})
  public void testAllListenerByScriptExcludingCompletion() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(7, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("delete").singleResult().getValue());
    assertEquals(1, query.variableName("deleteEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testFieldInjectionByClass.cmmn"})
  public void testFieldInjectionByClass() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertEquals("Hello from The Case", query.variableName("greeting").singleResult().getValue());
    assertEquals("Hello World", query.variableName("helloWorld").singleResult().getValue());
    assertEquals("cam", query.variableName("prefix").singleResult().getValue());
    assertEquals("unda", query.variableName("suffix").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testFieldInjectionByDelegateExpression.cmmn"})
  public void testFieldInjectionByDelegateExpression() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new FieldInjectionTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertEquals("Hello from The Case", query.variableName("greeting").singleResult().getValue());
    assertEquals("Hello World", query.variableName("helloWorld").singleResult().getValue());
    assertEquals("cam", query.variableName("prefix").singleResult().getValue());
    assertEquals("unda", query.variableName("suffix").singleResult().getValue());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/task/TaskListenerTest.testListenerByScriptResource.cmmn",
      "org/camunda/bpm/engine/test/task/taskListener.groovy"
      })
  public void testListenerByScriptResource() {
    // given
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    String taskId = taskService
        .createTaskQuery()
        .caseExecutionId(humanTaskId)
        .singleResult()
        .getId();

    taskService.setAssignee(taskId, "jonny");

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(7, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("assignment").singleResult().getValue());
    assertEquals(1, query.variableName("assignmentEventCounter").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testDoesNotImplementTaskListenerInterfaceByClass.cmmn"})
  public void testDoesNotImplementTaskListenerInterfaceByClass() {
    // given
    caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    try {
      caseService
        .withCaseExecution(humanTaskId)
        .manualStart();
    } catch (Exception e) {
      // then
      Throwable cause = e.getCause();
      String message = cause.getMessage();
      assertTextPresent("NotTaskListener doesn't implement "+TaskListener.class, message);
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testDoesNotImplementTaskListenerInterfaceByDelegateExpression.cmmn"})
  public void testDoesNotImplementTaskListenerInterfaceByDelegateExpression() {
    // given
    caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myTaskListener", new NotTaskListener())
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    try {
      caseService
        .withCaseExecution(humanTaskId)
        .manualStart();
    } catch (Exception e) {
      // then
      Throwable cause = e.getCause();
      String message = cause.getMessage();
      assertTextPresent("Delegate expression ${myTaskListener} did not resolve to an implementation of interface "+TaskListener.class.getName(), message);
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/task/TaskListenerTest.testTaskListenerDoesNotExist.cmmn"})
  public void testTaskListenerDoesNotExist() {
    // given
    caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    try {
      caseService
        .withCaseExecution(humanTaskId)
        .manualStart();
    } catch (Exception e) {
      // then
      Throwable cause = e.getCause();
      String message = cause.getMessage();
      assertTextPresent("Exception while instantiating class 'org.camunda.bpm.engine.test.task.NotExistingTaskListener'", message);
    }

  }

  protected void terminate(final String caseExecutionId) {
    processEngineConfiguration
      .getCommandExecutorTxRequired()
      .execute(new Command<Void>() {

        @Override
        public Void execute(CommandContext commandContext) {
          CmmnExecution caseTask = (CmmnExecution) caseService
              .createCaseExecutionQuery()
              .caseExecutionId(caseExecutionId)
              .singleResult();
          caseTask.terminate();
          return null;
        }

      });
  }

}
