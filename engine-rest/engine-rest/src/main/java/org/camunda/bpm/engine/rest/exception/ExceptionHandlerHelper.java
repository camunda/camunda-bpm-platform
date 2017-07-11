package org.camunda.bpm.engine.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.rest.dto.AuthorizationExceptionDto;
import org.camunda.bpm.engine.rest.dto.ExceptionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigratingProcessInstanceValidationExceptionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationPlanValidationExceptionDto;

/**
 * @author Svetlana Dorokhova.
 */
public class ExceptionHandlerHelper {

  private static ExceptionHandlerHelper INSTANCE = new ExceptionHandlerHelper();

  private ExceptionHandlerHelper() {
  }

  public static ExceptionHandlerHelper getInstance(){
    return INSTANCE;
  }

  public ExceptionDto fromException(Throwable e) {
    if (e instanceof MigratingProcessInstanceValidationException) {
      return MigratingProcessInstanceValidationExceptionDto.from((MigratingProcessInstanceValidationException)e);
    } else if (e instanceof MigrationPlanValidationException) {
      return MigrationPlanValidationExceptionDto.from((MigrationPlanValidationException)e);
    } else if (e instanceof AuthorizationException) {
      return AuthorizationExceptionDto.fromException((AuthorizationException)e);
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
      || exception instanceof BadUserRequestException) {
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
