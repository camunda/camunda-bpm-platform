package org.camunda.bpm.engine.rest.sub.runtime;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.rest.dto.runtime.IncidentDto;

public interface IncidentResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  IncidentDto getIncident();

  @DELETE
  void resolveIncident();
}