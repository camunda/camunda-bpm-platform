package org.camunda.bpm.qa.upgrade;

import org.junit.Assert;
import org.junit.Test;

public class TestJobSuspensionStateUpgrade extends AbstractDbUpgradeTestCase {

  @Test
  public void testCascadingSuspensionOfJobs() {
    final String key = "TestFixture70.testCascadingSuspensionOfJobs";

    // given a suspended process instance.
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(key).active().count());
    Assert.assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey(key).suspended().count());

    // then the job instance must be suspended as well.
    Assert.assertEquals(0, managementService.createJobQuery().processDefinitionKey(key).active().count());
    Assert.assertEquals(1, managementService.createJobQuery().processDefinitionKey(key).suspended().count());

    // if I activate the process instance
    runtimeService.activateProcessInstanceById(runtimeService.createProcessInstanceQuery().processDefinitionKey(key).singleResult().getId());

    // then the job is active as well
    Assert.assertEquals(1, managementService.createJobQuery().processDefinitionKey(key).active().count());
    Assert.assertEquals(0, managementService.createJobQuery().processDefinitionKey(key).suspended().count());

    // I can complete the process instance
    managementService.executeJob(managementService.createJobQuery().processDefinitionKey(key).singleResult().getId());
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(key).count());
  }
}
