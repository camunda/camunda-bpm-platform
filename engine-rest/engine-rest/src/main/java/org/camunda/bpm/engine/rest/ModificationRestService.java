package org.camunda.bpm.engine.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.ModificationDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;

@Produces(MediaType.APPLICATION_JSON)
public interface ModificationRestService {

  public static final String PATH = "/modification";
  
  @POST
  @Path("/execute")
  @Consumes(MediaType.APPLICATION_JSON)
  void executeModification(ModificationDto modificationExecutionDto);

  @POST
  @Path("/executeAsync")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BatchDto executeModificationAsync(ModificationDto modificationExecutionDto);
}
