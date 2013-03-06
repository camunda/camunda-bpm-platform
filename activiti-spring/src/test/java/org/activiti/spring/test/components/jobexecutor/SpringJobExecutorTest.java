package org.activiti.spring.test.components.jobexecutor;

import java.util.List;

import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Pablo Ganga
 */
@ContextConfiguration("classpath:org/activiti/spring/test/components/SpringjobExecutorTest-context.xml")
public class SpringJobExecutorTest extends SpringActivitiTestCase {
	
  @Deployment(resources={"org/activiti/spring/test/components/SpringTimersProcess.bpmn20.xml",
          "org/activiti/spring/test/components/SpringJobExecutorRollBack.bpmn20.xml"})
	public void testHappyJobExecutorPath()throws Exception {
		
		ProcessInstance instance = runtimeService.startProcessInstanceByKey("process1");
		
		assertNotNull(instance);
		
		waitForTasksToExpire();
		
		List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
		assertTrue(activeTasks.size() == 0);
	}
	
  @Deployment(resources={"org/activiti/spring/test/components/SpringTimersProcess.bpmn20.xml",
  "org/activiti/spring/test/components/SpringJobExecutorRollBack.bpmn20.xml"})
	public void testRollbackJobExecutorPath()throws Exception {
		
		ProcessInstance instance = runtimeService.startProcessInstanceByKey("errorProcess1");
		
		assertNotNull(instance);
		
		waitForTasksToExpire();
		
		List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
		assertTrue(activeTasks.size() == 1);
	}
	
	private void waitForTasksToExpire()throws Exception
	{
		Thread.sleep(2000L);
	}
	
}
