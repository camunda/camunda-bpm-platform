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
package org.camunda.bpm.engine.rest.sub.externaltask.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.externaltask.CompleteExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExtendLockOnExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskBpmnError;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.ExternalTaskFailureDto;
import org.camunda.bpm.engine.rest.dto.externaltask.LockExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.runtime.PriorityDto;
import org.camunda.bpm.engine.rest.dto.runtime.RetriesDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.externaltask.ExternalTaskResource;
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskResourceImpl implements ExternalTaskResource {

  protected ProcessEngine engine;
  protected String externalTaskId;
  protected ObjectMapper objectMapper;

  public ExternalTaskResourceImpl(ProcessEngine engine, String externalTaskId, ObjectMapper objectMapper) {
    this.engine = engine;
    this.externalTaskId = externalTaskId;
    this.objectMapper = objectMapper;
  }

  @Override
  public ExternalTaskDto getExternalTask() {
    ExternalTask task = engine
        .getExternalTaskService()
        .createExternalTaskQuery()
        .externalTaskId(externalTaskId)
        .singleResult();

    if (task == null) {
      throw new RestException(Status.NOT_FOUND, "External task with id " + externalTaskId + " does not exist");
    }

    return ExternalTaskDto.fromExternalTask(task);
  }

  @Override
  public String getErrorDetails() {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();

    try {
      return externalTaskService.getExternalTaskErrorDetails(externalTaskId);
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    }
  }

  @Override
  public void setRetries(RetriesDto dto) {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();
    Integer retries = dto.getRetries();

    if (retries == null) {
      throw new InvalidRequestException(Status.BAD_REQUEST, "The number of retries cannot be null.");
    }

    try {
      externalTaskService.setRetries(externalTaskId, retries);
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    }
  }

  @Override
  public void setPriority(PriorityDto dto) {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();

    try {
      externalTaskService.setPriority(externalTaskId, dto.getPriority());
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    }
  }

  @Override
  public void complete(CompleteExternalTaskDto dto) {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();

    VariableMap variables = VariableValueDto.toMap(dto.getVariables(), engine, objectMapper);
    VariableMap localVariables = VariableValueDto.toMap(dto.getLocalVariables(), engine, objectMapper);

    try {
      externalTaskService.complete(externalTaskId, dto.getWorkerId(), variables, localVariables);
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    } catch (BadUserRequestException e) {
      throw new RestException(Status.BAD_REQUEST, e, e.getMessage());
    }

  }

  @Override
  public void handleFailure(ExternalTaskFailureDto dto) {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();

    try {
      externalTaskService.handleFailure(externalTaskId,
          dto.getWorkerId(),
          dto.getErrorMessage(),
          dto.getErrorDetails(),
          dto.getRetries(),
          dto.getRetryTimeout(),
          VariableValueDto.toMap(dto.getVariables(), engine, objectMapper),
          VariableValueDto.toMap(dto.getLocalVariables(), engine, objectMapper));
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    } catch (BadUserRequestException e) {
      throw new RestException(Status.BAD_REQUEST, e, e.getMessage());
    }
  }

  @Override
  public void handleBpmnError(ExternalTaskBpmnError dto) {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();

    try {
      externalTaskService.handleBpmnError(externalTaskId, dto.getWorkerId(), dto.getErrorCode(), dto.getErrorMessage(), VariableValueDto.toMap(dto.getVariables(), engine, objectMapper));
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    } catch (BadUserRequestException e) {
      throw new RestException(Status.BAD_REQUEST, e, e.getMessage());
    }
  }

  @Override
  public void lock(LockExternalTaskDto lockExternalTaskDto) {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();

    try {
      externalTaskService
          .lock(externalTaskId, lockExternalTaskDto.getWorkerId(), lockExternalTaskDto.getLockDuration());
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    } catch (BadUserRequestException e) {
      throw new RestException(Status.BAD_REQUEST, e, e.getMessage());
    }
  }

  @Override
  public void extendLock(ExtendLockOnExternalTaskDto extendLockDto) {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();

    try {
      externalTaskService.extendLock(externalTaskId, extendLockDto.getWorkerId(), extendLockDto.getNewDuration());
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    } catch (BadUserRequestException e) {
      throw new RestException(Status.BAD_REQUEST, e, e.getMessage());
    }
  }

  @Override
  public void unlock() {
    ExternalTaskService externalTaskService = engine.getExternalTaskService();

    try {
      externalTaskService.unlock(externalTaskId);
    } catch (NotFoundException e) {
      throw new RestException(Status.NOT_FOUND, e, "External task with id " + externalTaskId + " does not exist");
    }
  }
}
