package org.camunda.bpm.engine.rest.application;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.rest.exception.RestException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Does not declare produced media types.
 * @author Thorben Lindhauer
 *
 */
@Path("/unannotated")
public class UnannotatedResource {

  @GET
  @Path("/exception")
  public String throwAnException() throws Exception {
    throw new Exception("expected exception");
  }

  @GET
  @Path("/processEngineException")
  public String throwProcessEngineException() throws Exception {
    throw new ProcessEngineException("expected exception");
  }

  @GET
  @Path("/restException")
  public String throwRestException() throws Exception {
    throw new RestException(Status.BAD_REQUEST, "expected exception");
  }

  @GET
  @Path("/authorizationException")
  public String throwAuthorizationException() throws Exception {
    throw new AuthorizationException("someUser", "somePermission", "someResourceName", "someResourceId");
  }

  @GET
  @Path("/stackOverflowError")
  public String throwStackOverflowError() throws Throwable {
    throw new StackOverflowError("Stack overflow");
  }

  @GET
  @Path("/authorizationExceptionMultiple")
  public String throwAuthorizationExceptionMultiple() throws Exception {
    List<MissingAuthorization> missingAuthorizations = new ArrayList<MissingAuthorization>();

    missingAuthorizations.add(
        new MissingAuthorization("somePermission1", "someResourceName1", "someResourceId1"));
    missingAuthorizations.add(
        new MissingAuthorization("somePermission2", "someResourceName2", "someResourceId2"));
    throw new AuthorizationException("someUser", missingAuthorizations);
  }
}
