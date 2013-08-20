package org.camunda.bpm.qa.upgrade;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Test;

public class TestAsyncContinuation extends AbstractDbUpgradeTestCase {

  @Test
  public void testAsyncContinuation() {
    String processDefinitionKey = "TestFixture62.asyncContinuationProcess";
    
    ProcessInstance asyncInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).singleResult();
    Job job = managementService.createJobQuery().processInstanceId(asyncInstance.getId()).singleResult();
    
    Assert.assertNotNull(job);
    managementService.executeJob(job.getId());
    
    asyncInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).singleResult();
    Assert.assertNull("the process instance should have finished successfully", asyncInstance);
  }
}
