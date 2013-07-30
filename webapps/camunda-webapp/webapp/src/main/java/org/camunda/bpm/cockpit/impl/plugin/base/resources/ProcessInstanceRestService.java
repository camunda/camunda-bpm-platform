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
package org.camunda.bpm.cockpit.impl.plugin.base.resources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessInstanceResource;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.rest.dto.CountResultDto;

public class ProcessInstanceRestService extends AbstractPluginResource {

  public static final String PATH = "/process-instance";

  public ProcessInstanceRestService(String engineName) {
    super(engineName);
  }

  @Path("/{id}")
  public ProcessInstanceResource getProcessInstance(@PathParam("id") String id) {
    return new ProcessInstanceResource(getProcessEngine().getName(), id);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProcessInstanceDto> getProcessInstances(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults) {
    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto(uriInfo.getQueryParameters());
    return queryProcessInstances(queryParameter, firstResult, maxResults);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<ProcessInstanceDto> queryProcessInstances(ProcessInstanceQueryDto queryParameter,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults) {

    injectEngineConfig(queryParameter);
    paginate(queryParameter, firstResult, maxResults);
    
    return getQueryService().executeQuery("selectRunningProcessInstancesIncludingIncidents", queryParameter);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/count")
  public CountResultDto getProcessInstancesCount(@Context UriInfo uriInfo) {
    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto(uriInfo.getQueryParameters());
    return queryProcessInstancesCount(queryParameter);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/count")
  public CountResultDto queryProcessInstancesCount(ProcessInstanceQueryDto queryParameter) {

    injectEngineConfig(queryParameter);

    long result = getQueryService().executeQueryRowCount("selectRunningProcessInstancesCount", queryParameter);
    return new CountResultDto(result);
  }

  private void paginate(ProcessInstanceQueryDto queryParameter, Integer firstResult, Integer maxResults) {
    if (firstResult == null) {
      firstResult = 0;
    }
    if (maxResults == null) {
      maxResults = Integer.MAX_VALUE;
    }
    queryParameter.setFirstResult(firstResult);
    queryParameter.setMaxResults(maxResults);
  }

  private void injectEngineConfig(ProcessInstanceQueryDto parameter) {

    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) getProcessEngine()).getProcessEngineConfiguration();
    if (processEngineConfiguration.getHistoryLevel() == 0) {
      parameter.setHistoryEnabled(false);
    }

    parameter.initQueryVariableValues(processEngineConfiguration.getVariableTypes());
  }
}
