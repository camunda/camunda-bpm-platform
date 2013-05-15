package org.camunda.bpm.cockpit.plugin.base.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.cockpit.db.QueryParameters;
import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;

public class ProcessDefinitionResource extends AbstractPluginResource {

  public static final String PATH = "/process-definition";

  public ProcessDefinitionResource(String engineName) {
    super(engineName);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProcessDefinitionDto> getProcessDefinitions() {
    return getQueryService().executeQuery("selectProcessDefinitionWithFailedJobs", new QueryParameters<ProcessDefinitionDto>());
  }
}
