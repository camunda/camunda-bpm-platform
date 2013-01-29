package com.camunda.fox.platform.test.jobexecutor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.jobexecutor.beans.FailingSLSB;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

@RunWith(Arquillian.class)
public class FailedJobCommandTest extends AbstractFoxPlatformIntegrationTest {
  
  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment()
      .addClass(FailingSLSB.class)
      .addAsResource("com/camunda/fox/platform/test/jobexecutor/FailedJobCommandTest.bpmn20.xml");
      
  }
  
  @Test
  public void testJobRetriesDecremented() {
    runtimeService.startProcessInstanceByKey("theProcess");
    
    Assert.assertEquals(1, managementService.createJobQuery().withRetriesLeft().count());
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    // now the retries = 0
    
    Assert.assertEquals(0, managementService.createJobQuery().withRetriesLeft().count());
    
  }

}
