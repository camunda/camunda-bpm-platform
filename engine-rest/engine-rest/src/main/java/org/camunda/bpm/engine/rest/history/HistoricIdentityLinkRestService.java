package org.camunda.bpm.engine.rest.history;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIdentityLinkDto;

/**
 * @author Deivarayan Azhagappan
 *
 */
@Path(HistoricIdentityLinkRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricIdentityLinkRestService {

  public static final String PATH = "/identity-links";

  /**
   * Exposes the {@link HistoricIdentityLinkQuery} interface as a REST service.
   *
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricIdentityLinkDto> getHistoricIdentityLinks(@Context UriInfo uriInfo, @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getHistoricIdentityLinksCount(@Context UriInfo uriInfo);

}
