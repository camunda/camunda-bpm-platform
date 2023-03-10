/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.sub.runtime.impl;

import javax.ws.rs.core.Response.Status;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.management.SetJobRetriesBuilder;
import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDuedateDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobSuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.runtime.PriorityDto;
import org.camunda.bpm.engine.rest.dto.runtime.RetriesDto;
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
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    }
  }

  @Override
  public void setJobRetries(RetriesDto dto) {
    try {
      ManagementService managementService = engine.getManagementService();
      SetJobRetriesBuilder builder = managementService
          .setJobRetries(dto.getRetries())
          .jobId(jobId);
      if (dto.isDueDateSet()) {
        builder.dueDate(dto.getDueDate());
      }
      builder.execute();
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Override
  public void executeJob() {
    try {
      ManagementService managementService = engine.getManagementService();
      managementService.executeJob(this.jobId);
    } catch (AuthorizationException e) {
      throw e;
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
      managementService.setJobDuedate(jobId, dto.getDuedate(), dto.isCascade());
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Override
  public void recalculateDuedate(boolean creationDateBased) {
    try {
      ManagementService managementService = engine.getManagementService();
      managementService.recalculateJobDuedate(jobId, creationDateBased);
    } catch (AuthorizationException e) {
      throw e;
    } catch(NotFoundException e) {// rewrite status code from bad request (400) to not found (404)
      throw new InvalidRequestException(Status.NOT_FOUND, e, e.getMessage());
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public void updateSuspensionState(JobSuspensionStateDto dto) {
    dto.setJobId(jobId);
    dto.updateSuspensionState(engine);
  }

  @Override
  public void setJobPriority(PriorityDto dto) {
    if (dto.getPriority() == null) {
      throw new RestException(Status.BAD_REQUEST, "Priority for job '" + jobId + "' cannot be null.");
    }

    try {
      ManagementService managementService = engine.getManagementService();
      managementService.setJobPriority(jobId, dto.getPriority());
    } catch (AuthorizationException e) {
      throw e;
    } catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public void deleteJob() {
    try {
      engine.getManagementService()
        .deleteJob(jobId);
    } catch (AuthorizationException e) {
      throw e;
    } catch (NullValueException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

}
