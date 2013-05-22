package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.rest.JobRestService;

public class JobRestServiceImpl extends AbstractRestProcessEngineAware
		implements JobRestService {

	public JobRestServiceImpl() {
		super();
	}

	public JobRestServiceImpl(String engineName) {
		super(engineName);
	}

	@Override
	public void setJobRetries(String jobId, int retries) {
		ManagementService managementService = getProcessEngine()
				.getManagementService();
		managementService.setJobRetries(jobId, retries);
	}

}
