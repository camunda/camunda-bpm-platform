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
package org.camunda.bpm.tasklist.test.sample.simple.resources;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.camunda.bpm.admin.test.sample.simple.SimpleAdminPlugin;
import org.camunda.bpm.tasklist.resource.AbstractTasklistPluginRootResource;
import org.camunda.bpm.tasklist.test.sample.simple.SimpleTasklistPlugin;

/**
 * @author Roman Smirnov
 *
 */
@Path("plugin/" + SimpleTasklistPlugin.ID)
public class SimpleTasklistRootResource extends AbstractTasklistPluginRootResource {

  public SimpleTasklistRootResource() {
    super(SimpleAdminPlugin.ID);
  }

  @Path("{engine}/test")
  public SimpleTasklistResource getTestResource(@PathParam("engine") String engine) {
    return subResource(new SimpleTasklistResource(engine), engine);
  }

}
