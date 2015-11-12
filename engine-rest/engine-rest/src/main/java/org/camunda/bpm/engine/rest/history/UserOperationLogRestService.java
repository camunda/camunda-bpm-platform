package org.camunda.bpm.engine.rest.history;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogEntryDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Exposes the {@link org.camunda.bpm.engine.history.UserOperationLogQuery} as REST service.
 *
 * @author Danny Gräf
 */
@Path(UserOperationLogRestService.PATH)
public interface UserOperationLogRestService {

  public static final String PATH = "/user-operation";

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryUserOperationCount(@Context UriInfo uriInfo);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<UserOperationLogEntryDto> queryUserOperationEntries(@Context UriInfo uriInfo,
                                                           @QueryParam("firstResult") Integer firstResult,
                                                           @QueryParam("maxResults") Integer maxResults);
}
