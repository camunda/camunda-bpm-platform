package org.camunda.bpm.qa.upgrade;

import org.junit.Assert;
import org.junit.Test;

public class TestTaskSuspensionStateUpgrade extends AbstractDbUpgradeTestCase {

  @Test
  public void testCascadingSuspensionOfTasks() {
    Assert.assertEquals(1, taskService.createTaskQuery().processInstanceBusinessKey("suspendedInstance").suspended().count());
    Assert.assertEquals(0, taskService.createTaskQuery().processInstanceBusinessKey("suspendedInstance").active().count());
    
    Assert.assertEquals(0, taskService.createTaskQuery().processInstanceBusinessKey("activeInstance").suspended().count());
    Assert.assertEquals(1, taskService.createTaskQuery().processInstanceBusinessKey("activeInstance").active().count());
  }
}
