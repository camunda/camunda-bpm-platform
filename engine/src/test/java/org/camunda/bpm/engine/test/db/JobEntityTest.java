package org.camunda.bpm.engine.test.db;

import java.util.List;

import org.camunda.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * 
 * @author Clint Manning
 */
public class JobEntityTest extends PluggableProcessEngineTestCase {

	  @Deployment(resources={"org/camunda/bpm/engine/test/db/processWithGatewayAndTwoEndEvents.bpmn20.xml"})
	  public void testGatewayWithTwoEndEventsLastJobReAssignedToParentExe() {
	    
	    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();    
	    ProcessInstance pi = runtimeService.startProcessInstanceByKey(pd.getKey());
	 	String processInstanceId = pi.getProcessInstanceId();	
	 	
	    List<Job> jobList = managementService.createJobQuery().processInstanceId(processInstanceId).list();
	    assertNotNull(jobList);
	    assertEquals(2,jobList.size());
	    
	    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
	    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
	
	    AcquiredJobs acquiredJobs = commandExecutor.execute(new AcquireJobsCmd(jobExecutor));
	    List<List<String>> jobIdsList = acquiredJobs.getJobIdBatches();
	    List<String> jobIds = jobIdsList.get(0);
        commandExecutor.execute(new ExecuteJobsCmd(jobIds.get(0)));
	    
	    // There should be only one job left
	    jobList = managementService.createJobQuery().list();	
	    assertEquals(1, jobList.size());	    
	    // There should only be 1 execution left - the root execution	    
	    assertEquals(1, runtimeService.createExecutionQuery().list().size());
	    
	    // root execution should be attached to the last job
	    assertEquals(processInstanceId,jobList.get(0).getExecutionId());
	  
	    commandExecutor.execute(new ExecuteJobsCmd(jobIds.get(1)));
	  
	    // There should be no more jobs
	    jobList = managementService.createJobQuery().list();
	    assertEquals(0, jobList.size());	
	  }	  
	  
	  
	  @Deployment(resources={"org/camunda/bpm/engine/test/db/processGatewayAndTwoEndEventsPlusTimer.bpmn20.xml"})
	  public void testGatewayWithTwoEndEventsLastTimerReAssignedToParentExe() {
	    
	    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();    
	    runtimeService.startProcessInstanceByKey(pd.getKey());
	 	
	 	waitForJobExecutorToProcessAllJobs(1000);   
	    assertEquals(0,managementService.createJobQuery().list().size());	
	  }	  
}
