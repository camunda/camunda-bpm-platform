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
package org.camunda.bpm.cockpit.plugin.base.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.cockpit.plugin.base.persistence.entity.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;
import org.camunda.bpm.engine.repository.ProcessDefinition;

public class ProcessDefinitionResource extends AbstractPluginResource {

  public static final String PATH = "/process-definition";

  public ProcessDefinitionResource(String engineName) {
    super(engineName);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProcessDefinitionDto> getProcessDefinitions() {
    // Get latest versions of ProcessDefinitions 
    List<ProcessDefinition> processDefinitions = getProcessEngine().getRepositoryService().createProcessDefinitionQuery().latestVersion().list();
    
    List<ProcessDefinitionDto> result = new ArrayList<ProcessDefinitionDto>();
    
    for (ProcessDefinition processDefinition : processDefinitions) {
      ProcessDefinitionDto dto = new ProcessDefinitionDto();
      
      dto.setId(processDefinition.getId());
      dto.setKey(processDefinition.getKey());
      dto.setName(processDefinition.getName());
      
      result.add(dto);
    }
    
    return result;
  }
}
