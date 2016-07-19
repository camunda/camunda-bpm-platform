package org.camunda.bpm.engine.rest.history;

import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceReportResultDto;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Stefan Hentschel.
 */
@Produces(MediaType.APPLICATION_JSON)
public interface HistoricTaskInstanceReportService {

  String PATH = "/report";

  /**
   * creates a historic task instance report
   */
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<HistoricTaskInstanceReportResultDto> getTaskReportResults(@Context UriInfo uriInfo);
}
