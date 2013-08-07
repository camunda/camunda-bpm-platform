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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessDefinitionQueryDto;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;

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
    ProcessDefinitionQueryDto queryParameter = new ProcessDefinitionQueryDto(uriInfo.getQueryParameters());
    return queryCalledProcessDefinitions(queryParameter);
  }

  @POST
  @Path("/called-process-definitions")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<ProcessDefinitionDto> queryCalledProcessDefinitions(ProcessDefinitionQueryDto queryParameter) {  
    queryParameter.setParentProcessDefinitionId(id);
    injectEngineConfig(queryParameter);
    List<ProcessDefinitionDto> result = getQueryService().executeQuery("selectCalledProcessDefinitions", queryParameter);

    return result;
  }
  
  private void injectEngineConfig(ProcessDefinitionQueryDto parameter) {

    ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) getProcessEngine()).getProcessEngineConfiguration();
    if (processEngineConfiguration.getHistoryLevel() == 0) {
      parameter.setHistoryEnabled(false);
    }

    parameter.initQueryVariableValues(processEngineConfiguration.getVariableTypes());
  }
  
}
