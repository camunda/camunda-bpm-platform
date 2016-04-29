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
package org.camunda.bpm.admin.impl.plugin.resources;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.camunda.bpm.admin.impl.plugin.AdminPlugins;
import org.camunda.bpm.admin.resource.AbstractAdminPluginRootResource;

/**
 * @author vale
 */
@Path("plugin/" + AdminPlugins.ID)
public class AdminPluginsRootResource extends AbstractAdminPluginRootResource {

  public AdminPluginsRootResource() {
    super(AdminPlugins.ID);
  }

}
