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
import org.camunda.bpm.engine.rest.dto.history.HistoricIdentityLinkLogDto;

/**
 * @author Deivarayan Azhagappan
 *
 */
@Path(HistoricIdentityLinkLogRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricIdentityLinkLogRestService {

  public static final String PATH = "/identity-link-log";

  /**
   * Exposes the {@link HistoricIdentityLinkLogQuery} interface as a REST service.
   *
   * @param query
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricIdentityLinkLogDto> getHistoricIdentityLinks(@Context UriInfo uriInfo, @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getHistoricIdentityLinksCount(@Context UriInfo uriInfo);

}
