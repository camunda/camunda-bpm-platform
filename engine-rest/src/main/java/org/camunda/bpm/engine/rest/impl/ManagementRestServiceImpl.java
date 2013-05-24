package org.camunda.bpm.engine.rest.impl;

import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.rest.ManagementRestService;
import org.camunda.bpm.engine.runtime.Job;

public class ManagementRestServiceImpl extends AbstractRestProcessEngineAware
		implements ManagementRestService {

	public ManagementRestServiceImpl() {
		super();
	}

	public ManagementRestServiceImpl(String engineName) {
		super(engineName);
	}

	@Override
	public void setJobRetries(String jobId, int retries) {
		ManagementService managementService = getProcessEngine()
				.getManagementService();
		managementService.setJobRetries(jobId, retries);
	}

	@Override
	public void deleteJobs(String processInstanceId) {	
		ManagementService managementService = getProcessEngine().getManagementService();
		List<Job> processJobs = managementService.createJobQuery().processInstanceId(processInstanceId).list();
		for(Job job : processJobs){
			managementService.deleteJob(job.getId());				
		}		
	}

}
