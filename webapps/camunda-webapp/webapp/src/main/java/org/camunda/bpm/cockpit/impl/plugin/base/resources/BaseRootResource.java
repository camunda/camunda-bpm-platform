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

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.camunda.bpm.cockpit.impl.plugin.base.BasePlugin;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginRootResource;

/**
 * Root resource of the base plugin
 * 
 * @author nico.rehwaldt
 */
@Path("plugin/" + BasePlugin.ID)
public class BaseRootResource extends AbstractPluginRootResource {

  public BaseRootResource() {
    super(BasePlugin.ID);
  }
  
  @Path("{engine}" + ProcessDefinitionRestService.PATH)
  public ProcessDefinitionRestService getProcessDefinitionResource(@PathParam("engine") String engineName) {
    return subResource(new ProcessDefinitionRestService(engineName), engineName);
  }

  @Path("{engine}" + ProcessInstanceRestService.PATH)
  public ProcessInstanceRestService getProcessInstanceRestService(@PathParam("engine") String engineName) {
    return subResource(new ProcessInstanceRestService(engineName), engineName);
  }
  
  @Path("{engine}" + IncidentRestService.PATH)
  public IncidentRestService getIncidentRestService(@PathParam("engine") String engineName) {
    return subResource(new IncidentRestService(engineName), engineName);
  }

}
