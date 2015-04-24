package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Assert;
import org.junit.Test;

public class TestUserTaskMigration extends AbstractDbUpgradeTestCase {

  @Test
  public void testVariableMigration() {
    final String key = "TestFixtureOld.testUserTaskMigration";

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

    // get process instance
    ProcessInstance pi = processInstanceQuery.processDefinitionKey(key).singleResult();

    // task
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(pi.getId());
    Task task = taskQuery.singleResult();
    Assert.assertNotNull(task);

    // complete task
    taskService.complete(task.getId());

    // the process instance is completed
    Assert.assertEquals(0, processInstanceQuery.count());

  }

}
