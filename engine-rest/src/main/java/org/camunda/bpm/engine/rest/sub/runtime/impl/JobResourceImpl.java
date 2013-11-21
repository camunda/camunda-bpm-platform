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
package org.camunda.bpm.engine.rest.sub.runtime.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDuedateDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobRetriesDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobSuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.runtime.JobResource;
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
    Job job = managementService.createJobQuery().jobId(jobId).singleResult();

    if (job == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Job with id " + jobId + " does not exist");
    }

    return JobDto.fromJob(job);
  }

  @Override
  public String getStacktrace() {
    try {
      ManagementService managementService = engine.getManagementService();
      String stacktrace = managementService.getJobExceptionStacktrace(jobId);
      return stacktrace;
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    }
  }

  @Override
  public void setJobRetries(JobRetriesDto dto) {
    try {
      ManagementService managementService = engine.getManagementService();
      managementService.setJobRetries(jobId, dto.getRetries());
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Override
  public void executeJob() {
    try {
      ManagementService managementService = engine.getManagementService();
      managementService.executeJob(this.jobId);
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    } catch (RuntimeException r) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, r.getMessage());
    }
  }

  @Override
  public void setJobDuedate(JobDuedateDto dto) {
    try {
      ManagementService managementService = engine.getManagementService();
      managementService.setJobDuedate(jobId, dto.getDuedate());
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public void updateSuspensionState(JobSuspensionStateDto dto) {
    dto.setJobId(jobId);
    dto.updateSuspensionState(engine);
  }

}
