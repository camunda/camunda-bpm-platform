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
package org.camunda.bpm.engine.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ParseException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.rest.dto.AuthorizationExceptionDto;
import org.camunda.bpm.engine.rest.dto.ExceptionDto;
import org.camunda.bpm.engine.rest.dto.ParseExceptionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigratingProcessInstanceValidationExceptionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanValidationExceptionDto;

/**
 * @author Svetlana Dorokhova.
 */
public class ExceptionHandlerHelper {

  protected static final ExceptionLogger LOGGER = ExceptionLogger.REST_LOGGER;
  protected static ExceptionHandlerHelper INSTANCE = new ExceptionHandlerHelper();

  private ExceptionHandlerHelper() {
  }

  public static ExceptionHandlerHelper getInstance(){
    return INSTANCE;
  }

  public Response getResponse(Throwable throwable) {
    LOGGER.log(throwable);

    Response.Status responseStatus = getStatus(throwable);
    ExceptionDto exceptionDto = fromException(throwable);

    provideExceptionCode(throwable, exceptionDto);

    return Response
        .status(responseStatus)
        .entity(exceptionDto)
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  protected void provideExceptionCode(Throwable throwable, ExceptionDto exceptionDto) {
    Integer code = null;
    if (throwable instanceof ProcessEngineException) {
      code = getCode(throwable);

    } else if (throwable instanceof RestException) {
      Throwable cause = throwable.getCause();
      if (cause instanceof ProcessEngineException) {
        code = getCode(cause);

      }
    }

    if (code != null) {
      exceptionDto.setCode(code);
    }
  }

  protected Integer getCode(Throwable throwable) {
    ProcessEngineException pex = (ProcessEngineException) throwable;
    return pex.getCode();
  }

  public ExceptionDto fromException(Throwable e) {
    if (e instanceof MigratingProcessInstanceValidationException) {
      return MigratingProcessInstanceValidationExceptionDto.from((MigratingProcessInstanceValidationException)e);
    } else if (e instanceof MigrationPlanValidationException) {
      return MigrationPlanValidationExceptionDto.from((MigrationPlanValidationException)e);
    } else if (e instanceof AuthorizationException) {
      return AuthorizationExceptionDto.fromException((AuthorizationException)e);
    } else if (e instanceof ParseException){
      return ParseExceptionDto.fromException((ParseException) e);
    } else {
      return ExceptionDto.fromException(e);
    }
  }

  public Response.Status getStatus(Throwable exception) {
    Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;

    if (exception instanceof ProcessEngineException) {
      responseStatus = getStatus((ProcessEngineException)exception);
    }
    else if (exception instanceof RestException) {
      responseStatus = getStatus((RestException) exception);
    }
    else if (exception instanceof WebApplicationException) {
      //we need to check this, as otherwise the logic for processing WebApplicationException will be overridden
      final int statusCode = ((WebApplicationException) exception).getResponse().getStatus();
      responseStatus = Response.Status.fromStatusCode(statusCode);
    }
    return responseStatus;
  }

  public Response.Status getStatus(ProcessEngineException exception) {
    Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;

    // provide custom handling of authorization exception
    if (exception instanceof AuthorizationException) {
      responseStatus = Response.Status.FORBIDDEN;
    }
    else if (exception instanceof MigrationPlanValidationException
      || exception instanceof MigratingProcessInstanceValidationException
      || exception instanceof BadUserRequestException
      || exception instanceof ParseException) {
      responseStatus = Response.Status.BAD_REQUEST;
    }
    return responseStatus;
  }

  public Response.Status getStatus(RestException exception) {
    if (exception.getStatus() != null) {
      return exception.getStatus();
    }
    return Response.Status.INTERNAL_SERVER_ERROR;
  }
}
