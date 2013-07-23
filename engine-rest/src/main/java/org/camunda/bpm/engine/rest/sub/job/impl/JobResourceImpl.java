package org.camunda.bpm.engine.rest.sub.job.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobRetriesDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.job.JobResource;
import org.camunda.bpm.engine.runtime.Job;

public class JobResourceImpl implements JobResource {

	private ProcessEngine engine;
	private String jobId;

	public JobResourceImpl(ProcessEngine engine, String jobId) {
		this.engine = engine;
		this.jobId = jobId;
	}

	@Override
	public JobDto getJob() {
		ManagementService managementService = engine.getManagementService();
		Job job = managementService.createJobQuery().jobId(jobId)
				.singleResult();

		if (job == null) {
			throw new InvalidRequestException(Status.NOT_FOUND, "Job with id "
					+ jobId + " does not exist");
		}

		return JobDto.fromJob(job);
	}

	@Override
	public void setJobRetries(JobRetriesDto dto) {
		try {
			ManagementService managementService = engine.getManagementService();
			managementService.setJobRetries(jobId, dto.getRetries());
		} catch (ProcessEngineException e) {
			throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR,e.getMessage());
		}
	}

	@Override
	public void executeJob() {
		try {
			ManagementService managementService = engine.getManagementService();
			managementService.executeJob(this.jobId);	
		} catch (ProcessEngineException e) {
			throw new InvalidRequestException(Status.NOT_FOUND,e.getMessage());
		}catch (RuntimeException r) {
			throw new RestException(Status.INTERNAL_SERVER_ERROR,r.getMessage());
		}				
	}

}
