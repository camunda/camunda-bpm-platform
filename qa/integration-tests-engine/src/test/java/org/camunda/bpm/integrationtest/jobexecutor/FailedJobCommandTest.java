package org.camunda.bpm.integrationtest.jobexecutor;

import org.camunda.bpm.integrationtest.jobexecutor.beans.FailingSLSB;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class FailedJobCommandTest extends AbstractFoxPlatformIntegrationTest {
  
  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment()
      .addClass(FailingSLSB.class)
      .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/FailedJobCommandTest.bpmn20.xml");
      
  }
  
  @Test
  public void testJobRetriesDecremented() {
    runtimeService.startProcessInstanceByKey("theProcess");
    
    Assert.assertEquals(1, managementService.createJobQuery().withRetriesLeft().count());
    
    waitForJobExecutorToProcessAllJobs(30000);
    
    // now the retries = 0
    
    Assert.assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    Assert.assertEquals(1, managementService.createJobQuery().noRetriesLeft().count());
    
  }
  
  @Test
  public void testJobRetriesDecremented_multiple() {
    
    for(int i = 0; i < 50; i++) {
      runtimeService.startProcessInstanceByKey("theProcess");
    }
    
    Assert.assertEquals(50, managementService.createJobQuery().withRetriesLeft().count());
    
    waitForJobExecutorToProcessAllJobs(180000);
    
    // now the retries = 0
    
    Assert.assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    Assert.assertEquals(51, managementService.createJobQuery().noRetriesLeft().count());
    
  }

}
