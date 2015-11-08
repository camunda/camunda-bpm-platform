package org.camunda.bpm.engine.spring.test.components.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.spring.impl.test.SpringProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Pablo Ganga
 */
@ContextConfiguration("classpath:org/camunda/bpm/engine/spring/test/components/SpringjobExecutorTest-context.xml")
public class SpringJobExecutorTest extends SpringProcessEngineTestCase {

  @Deployment(resources={"org/camunda/bpm/engine/spring/test/components/SpringTimersProcess.bpmn20.xml",
          "org/camunda/bpm/engine/spring/test/components/SpringJobExecutorRollBack.bpmn20.xml"})
	public void testHappyJobExecutorPath()throws Exception {

		ProcessInstance instance = runtimeService.startProcessInstanceByKey("process1");

		assertNotNull(instance);

		waitForJobExecutorToProcessAllJobs(10000);

		List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
		assertTrue(activeTasks.size() == 0);
	}

  @Deployment(resources={"org/camunda/bpm/engine/spring/test/components/SpringTimersProcess.bpmn20.xml",
  "org/camunda/bpm/engine/spring/test/components/SpringJobExecutorRollBack.bpmn20.xml"})
  public void testRollbackJobExecutorPath()throws Exception {

    // shutdown job executor first, otherwise waitForJobExecutorToProcessAllJobs will not actually start it....
    processEngineConfiguration.getJobExecutor().shutdown();

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("errorProcess1");

    assertNotNull(instance);

    waitForJobExecutorToProcessAllJobs(10000);

    List<Task> activeTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
    assertTrue(activeTasks.size() == 1);
  }


}
