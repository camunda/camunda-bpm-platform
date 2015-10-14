/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.rest.*;
import org.camunda.bpm.engine.rest.dto.ProcessEngineDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.history.HistoryRestService;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.*;

@Path(NamedProcessEngineRestServiceImpl.PATH)
public class NamedProcessEngineRestServiceImpl extends AbstractProcessEngineRestServiceImpl {

  public static final String PATH = "/engine";

  @Override
  @Path("/{name}" + ProcessDefinitionRestService.PATH)
  public ProcessDefinitionRestService getProcessDefinitionService(@PathParam("name") String engineName) {
    return super.getProcessDefinitionService(engineName);
  }

  @Override
  @Path("/{name}" + ProcessInstanceRestService.PATH)
  public ProcessInstanceRestService getProcessInstanceService(@PathParam("name") String engineName) {
    return super.getProcessInstanceService(engineName);
  }

  @Override
  @Path("/{name}" + ExecutionRestService.PATH)
  public ExecutionRestService getExecutionService(@PathParam("name") String engineName) {
    return super.getExecutionService(engineName);
  }

  @Override
  @Path("/{name}" + TaskRestService.PATH)
  public TaskRestService getTaskRestService(@PathParam("name") String engineName) {
    return super.getTaskRestService(engineName);
  }

  @Override
  @Path("/{name}" + IdentityRestService.PATH)
  public IdentityRestService getIdentityRestService(@PathParam("name") String engineName) {
    return super.getIdentityRestService(engineName);
  }

  @Override
  @Path("/{name}" + MessageRestService.PATH)
  public MessageRestService getMessageRestService(@PathParam("name") String engineName) {
    return super.getMessageRestService(engineName);
  }

  @Override
  @Path("/{name}" + VariableInstanceRestService.PATH)
  public VariableInstanceRestService getVariableInstanceService(@PathParam("name") String engineName) {
    return super.getVariableInstanceService(engineName);
  }

  @Override
  @Path("/{name}" + JobDefinitionRestService.PATH)
  public JobDefinitionRestService getJobDefinitionRestService(@PathParam("name") String engineName) {
    return super.getJobDefinitionRestService(engineName);
  }

  @Override
  @Path("/{name}" + JobRestService.PATH)
  public JobRestService getJobRestService(@PathParam("name") String engineName) {
    return super.getJobRestService(engineName);
  }

  @Override
  @Path("/{name}" + GroupRestService.PATH)
  public GroupRestService getGroupRestService(@PathParam("name") String engineName) {
    return super.getGroupRestService(engineName);
  }

  @Override
  @Path("/{name}" + UserRestService.PATH)
  public UserRestService getUserRestService(@PathParam("name") String engineName) {
    return super.getUserRestService(engineName);
  }

  @Override
  @Path("/{name}" + AuthorizationRestService.PATH)
  public AuthorizationRestService getAuthorizationRestService(@PathParam("name") String engineName) {
    return super.getAuthorizationRestService(engineName);
  }

  @Override
  @Path("/{name}" + IncidentRestService.PATH)
  public IncidentRestService getIncidentService(@PathParam("name") String engineName) {
    return super.getIncidentService(engineName);
  }

  @Override
  @Path("/{name}" + HistoryRestService.PATH)
  public HistoryRestService getHistoryRestService(@PathParam("name") String engineName) {
    return super.getHistoryRestService(engineName);
  }

  @Override
  @Path("/{name}" + DeploymentRestService.PATH)
  public DeploymentRestService getDeploymentRestService(@PathParam("name") String engineName) {
    return super.getDeploymentRestService(engineName);
  }

  @Override
  @Path("/{name}" + CaseDefinitionRestService.PATH)
  public CaseDefinitionRestService getCaseDefinitionRestService(@PathParam("name") String engineName) {
    return super.getCaseDefinitionRestService(engineName);
  }

  @Override
  @Path("/{name}" + CaseInstanceRestService.PATH)
  public CaseInstanceRestService getCaseInstanceRestService(@PathParam("name") String engineName) {
    return super.getCaseInstanceRestService(engineName);
  }

  @Override
  @Path("/{name}" + CaseExecutionRestService.PATH)
  public CaseExecutionRestService getCaseExecutionRestService(@PathParam("name") String engineName) {
    return super.getCaseExecutionRestService(engineName);
  }

  @Override
  @Path("/{name}" + FilterRestService.PATH)
  public FilterRestService getFilterRestService(@PathParam("name") String engineName) {
    return super.getFilterRestService(engineName);
  }

  @Override
  @Path("/{name}" + MetricsRestService.PATH)
  public MetricsRestService getMetricsRestService(@PathParam("name") String engineName) {
    return super.getMetricsRestService(engineName);
  }

  @Override
  @Path("/{name}" + DecisionDefinitionRestService.PATH)
  public DecisionDefinitionRestService getDecisionDefinitionRestService(@PathParam("name") String engineName) {
    return super.getDecisionDefinitionRestService(engineName);
  }

  @Path("/{name}" + ExternalTaskRestService.PATH)
  public ExternalTaskRestService getExternalTaskRestService(@PathParam("name") String engineName) {
    return super.getExternalTaskRestService(engineName);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProcessEngineDto> getProcessEngineNames() {
    ProcessEngineProvider provider = getProcessEngineProvider();
    Set<String> engineNames = provider.getProcessEngineNames();

    List<ProcessEngineDto> results = new ArrayList<ProcessEngineDto>();
    for (String engineName : engineNames) {
      ProcessEngineDto dto = new ProcessEngineDto();
      dto.setName(engineName);
      results.add(dto);
    }

    return results;
  }

  @Override
  protected URI getRelativeEngineUri(String engineName) {
    return UriBuilder.fromResource(NamedProcessEngineRestServiceImpl.class).path("{name}").build(engineName);
  }

  protected ProcessEngineProvider getProcessEngineProvider() {
    ServiceLoader<ProcessEngineProvider> serviceLoader = ServiceLoader.load(ProcessEngineProvider.class);
    Iterator<ProcessEngineProvider> iterator = serviceLoader.iterator();

    if(iterator.hasNext()) {
      ProcessEngineProvider provider = iterator.next();
      return provider;
    } else {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, "No process engine provider found");
    }
  }

}
