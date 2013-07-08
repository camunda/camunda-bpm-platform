package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.JobRestService;
import org.camunda.bpm.engine.rest.dto.job.JobDto;
import org.camunda.bpm.engine.rest.dto.job.JobQueryDto;
import org.camunda.bpm.engine.rest.sub.job.JobResource;
import org.camunda.bpm.engine.rest.sub.job.impl.JobResourceImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;

public class JobRestServiceImpl extends AbstractRestProcessEngineAware
		implements JobRestService {

	public JobRestServiceImpl() {
		super();
	}

	public JobRestServiceImpl(String engineName) {
		super(engineName);
	}

	@Override
	public JobResource getJob(String jobId) {
		return new JobResourceImpl(getProcessEngine(), jobId);
	}

	@Override
	public List<JobDto> getJobs(UriInfo uriInfo, Integer firstResult,
			Integer maxResults) {
		JobQueryDto queryDto = new JobQueryDto(uriInfo.getQueryParameters());
		return queryJobs(queryDto, firstResult, maxResults);
	}

	@Override
	public List<JobDto> queryJobs(JobQueryDto queryDto, Integer firstResult,
			Integer maxResults) {
		ProcessEngine engine = getProcessEngine();
		JobQuery query = queryDto.toQuery(engine);

		List<Job> matchingJobs;
		if (firstResult != null || maxResults != null) {
			matchingJobs = executePaginatedQuery(query, firstResult, maxResults);
		} else {
			matchingJobs = query.list();
		}

		List<JobDto> jobResults = new ArrayList<JobDto>();
		for (Job job : matchingJobs) {
			JobDto resultJob = JobDto.fromJob(job);
			jobResults.add(resultJob);
		}
		return jobResults;
	}

	private List<Job> executePaginatedQuery(JobQuery query,
			Integer firstResult, Integer maxResults) {
		if (firstResult == null) {
			firstResult = 0;
		}
		if (maxResults == null) {
			maxResults = Integer.MAX_VALUE;
		}
		return query.listPage(firstResult, maxResults);
	}

}
