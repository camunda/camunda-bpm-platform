/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.JobRestService;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobSuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.runtime.JobResource;
import org.camunda.bpm.engine.rest.sub.runtime.impl.JobResourceImpl;
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

  @Override
  public CountResultDto getJobsCount(@Context UriInfo uriInfo) {
    JobQueryDto queryDto = new JobQueryDto(uriInfo.getQueryParameters());
    return queryJobsCount(queryDto);
  }

  @Override
  public CountResultDto queryJobsCount(JobQueryDto queryDto) {
    ProcessEngine engine = getProcessEngine();
    JobQuery query = queryDto.toQuery(engine);

    long count = query.count();
    CountResultDto result = new CountResultDto();
    result.setCount(count);

    return result;
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

  public void updateSuspensionState(JobSuspensionStateDto dto) {
    if (dto.getJobId() != null) {
      String message = "Either jobDefinitionId, processInstanceId, processDefinitionId or processDefinitionKey can be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    dto.updateSuspensionState(getProcessEngine());
  }

}
