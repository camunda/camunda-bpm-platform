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
package org.camunda.bpm.cockpit.impl.plugin.base.sub.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.bpm.engine.runtime.Incident;

public class ProcessInstanceResource extends AbstractPluginResource {
  
  protected String id;

  public ProcessInstanceResource(String engineName, String id) {
    super(engineName);
    this.id = id;
  }

  @GET
  @Path("/incidents")
  @Produces(MediaType.APPLICATION_JSON)
  public List<IncidentDto> getIncidents() {
    List<Incident> incidents =  getProcessEngine()
        .getRuntimeService()
        .createIncidentQuery()
        .processInstanceId(id)
        .list();
    
    return IncidentDto.fromListOfIncidents(incidents);
  }
  
  @GET
  @Path("/called-process-instances")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProcessInstanceDto> getCalledProcessInstances(@Context UriInfo uriInfo) {
    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto(uriInfo.getQueryParameters());
    return queryCalledProcessInstances(queryParameter);
  }
  
  @POST
  @Path("/called-process-instances")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProcessInstanceDto> queryCalledProcessInstances(ProcessInstanceQueryDto queryParameter) {
    queryParameter.setParentProcessInstanceId(id);
    return getQueryService().executeQuery("selectCalledProcessInstances", queryParameter);
  }

}
