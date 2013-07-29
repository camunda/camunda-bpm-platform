package org.camunda.bpm.cockpit.impl.plugin.base.resources;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessDefinitionResource;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;

public class ProcessDefinitionRestService extends AbstractPluginResource {

  public static final String PATH = "/process-definition";
  
  public ProcessDefinitionRestService(String engineName) {
    super(engineName);
  }
  
  @Path("/{id}")
  public ProcessDefinitionResource getProcessDefinition(@PathParam("id") String id) {
    return new ProcessDefinitionResource(getProcessEngine().getName(), id);
  }

}
