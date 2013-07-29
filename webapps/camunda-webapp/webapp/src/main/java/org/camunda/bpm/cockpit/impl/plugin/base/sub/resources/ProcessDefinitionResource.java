package org.camunda.bpm.cockpit.impl.plugin.base.sub.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.impl.plugin.base.query.parameter.ProcessDefinitionQueryParameter;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;

public class ProcessDefinitionResource extends AbstractPluginResource {

  protected String id;
  
  public ProcessDefinitionResource(String engineName, String id) {
    super(engineName);
    this.id = id;
  }
  
  @GET
  @Path("/called-process-definitions")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProcessDefinitionDto> getCalledProcessDefinitions(@Context UriInfo uriInfo) {
    ProcessDefinitionQueryParameter queryParameter = new ProcessDefinitionQueryParameter(uriInfo.getQueryParameters());
    return queryCalledProcessDefinitions(queryParameter);
  }

  @POST
  @Path("/called-process-definitions")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<ProcessDefinitionDto> queryCalledProcessDefinitions(ProcessDefinitionQueryParameter queryParameter) {  
    queryParameter.setParentProcessDefinitionId(id);
    List<ProcessDefinitionDto> result = getQueryService().executeQuery("selectCalledProcessDefinitions", queryParameter);
    return result;
  }
  
}
