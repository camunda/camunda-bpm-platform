package org.camunda.bpm.integrationtest.functional.classloading.war;

import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleTaskListener;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.integrationtest.util.TestContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TaskListenerResolutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createProcessArchiveDeplyoment() {    
    return initWebArchiveDeployment()
            .addClass(ExampleTaskListener.class)            
            .addAsResource("org/camunda/bpm/integrationtest/functional/classloading/TaskListenerResolutionTest.testResolveClassOnTaskComplete.bpmn20.xml");
  }
  
  @Deployment(name="clientDeployment")
  public static WebArchive clientDeployment() {    
    WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "client.war")
            .addClass(AbstractFoxPlatformIntegrationTest.class);
    
    TestContainer.addContainerSpecificResources(webArchive);
    
    return webArchive;
            
  }
  
  @Test
  @OperateOnDeployment("clientDeployment")
  public void testResolveClassOnTaskComplete() {   
    // assert that we cannot load the delegate here:
    try {
      Class.forName("org.camunda.bpm.integrationtest.functional.classloading.beans.ExampleTaskListener");
      Assert.fail("CNFE expected");
    }catch (ClassNotFoundException e) {
      // expected
    }
    
    runtimeService.startProcessInstanceByKey("testTaskListenerProcess");    
    
    // the listener should execute successfully
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    
    Execution execution = runtimeService.createExecutionQuery().singleResult();
    Assert.assertNotNull(runtimeService.getVariable(execution.getId(), "listener"));
    
    // the delegate expression listener should execute successfully
    runtimeService.removeVariable(execution.getId(), "listener");
    
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    
    Assert.assertNotNull(runtimeService.getVariable(execution.getId(), "listener"));
    
  }
}
