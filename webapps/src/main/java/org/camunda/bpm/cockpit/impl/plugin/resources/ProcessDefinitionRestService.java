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
package org.camunda.bpm.cockpit.impl.plugin.resources;

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
