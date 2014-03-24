package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Assert;
import org.junit.Test;

public class TestVariableMigration extends AbstractDbUpgradeTestCase {

  @Test
  public void testVariableMigration() {
    final String key = "TestFixture70.testVariableMigration";

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

    // get process instance
    ProcessInstance pi = processInstanceQuery.processDefinitionKey(key).singleResult();

    VariableInstanceQuery variableInstanceQuery = getClearVariableInstanceQuery(pi.getId());
    ExecutionQuery executionQuery = runtimeService.createExecutionQuery().processInstanceId(pi.getId());
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(pi.getId());
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());

    Task task = taskQuery.singleResult();

    // there should be five variable instance (3 process instance variables) and...
    Assert.assertEquals(5, variableInstanceQuery.count());

    // ...2 task variable
    Assert.assertEquals(2, variableInstanceQuery.taskIdIn(task.getId()).count());

    // add a new task variable
    taskService.setVariableLocal(task.getId(), "aNewLocalTaskVariableName", "aNewLockTaskVariableValue");

    // change task variable value
    taskService.setVariableLocal(task.getId(), "aLocalTaskVariableName", "aNewValue");

    Assert.assertEquals(3, variableInstanceQuery.taskIdIn(task.getId()).count());

    // complete task
    taskService.complete(task.getId());

    variableInstanceQuery = getClearVariableInstanceQuery(pi.getId());

    // there should be three variables...
    Assert.assertEquals(3, variableInstanceQuery.count());

    Execution execution = executionQuery.singleResult();

    // update existing variable
    runtimeService.setVariableLocal(execution.getId(), "anotherVariableName", "aNewFinalValue");

    Job job = jobQuery.singleResult();
    managementService.executeJob(job.getId());

    // the process instance is completed
    Assert.assertEquals(0, processInstanceQuery.count());

  }

  private VariableInstanceQuery getClearVariableInstanceQuery(String processInstanceId) {
    return runtimeService
        .createVariableInstanceQuery()
        .processInstanceIdIn(processInstanceId);
  }

}
