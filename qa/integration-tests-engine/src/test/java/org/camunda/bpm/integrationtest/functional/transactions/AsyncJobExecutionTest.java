package org.camunda.bpm.integrationtest.functional.transactions;

import java.util.HashMap;

import javax.inject.Inject;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class AsyncJobExecutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addClass(GetVersionInfoDelegate.class)
            .addClass(UpdateRouterConfiguration.class)
            .addAsResource("org/camunda/bpm/integrationtest/functional/transactions/AsyncJobExecutionTest.testAsyncServiceTasks.bpmn20.xml")
            .addAsWebInfResource("persistence.xml", "classes/META-INF/persistence.xml");
  }

  @Inject
  private RuntimeService runtimeService;

  @Test
  public void testAsyncServiceTasks() {
    try {
      HashMap<String, Object> variables = new HashMap<String, Object>();
      variables.put("serialnumber", "23");
      runtimeService.startProcessInstanceByKey("configure-router", variables);
      
      waitForJobExecutorToProcessAllJobs(6000);
      
    } catch (Exception ex) {
      Assert.fail("Unexpected exception!");
    }
  }
}
