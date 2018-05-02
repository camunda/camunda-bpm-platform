package org.camunda.bpm.engine.rest.history;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.history.HistoryCleanupConfigurationDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDto;

public interface HistoryCleanupRestService {

  public static final String PATH = "/cleanup";

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  JobDto cleanupAsync(@QueryParam("immediatelyDue") @DefaultValue("true") boolean immediatelyDue);

  @GET
  @Path("/job")
  @Produces(MediaType.APPLICATION_JSON)
  JobDto findCleanupJob();

  @GET
  @Path("/jobs")
  @Produces(MediaType.APPLICATION_JSON)
  List<JobDto> findCleanupJobs();

  @GET
  @Path("/configuration")
  @Produces(MediaType.APPLICATION_JSON)
  HistoryCleanupConfigurationDto getHistoryCleanupConfiguration();
}
