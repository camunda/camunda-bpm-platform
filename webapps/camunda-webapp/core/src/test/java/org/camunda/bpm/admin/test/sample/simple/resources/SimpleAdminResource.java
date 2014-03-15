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
package org.camunda.bpm.admin.test.sample.simple.resources;

import java.util.Map;

import javax.ws.rs.GET;

import org.camunda.bpm.admin.resource.AbstractAdminPluginResource;

/**
 * @author Daniel Meyer
 *
 */
public class SimpleAdminResource extends AbstractAdminPluginResource {

  public SimpleAdminResource(String engineName) {
    super(engineName);
  }

  @GET
  public Map<String, String> listProperties() {

    Map<String, String> properties = getProcessEngine().getManagementService().getProperties();

    return properties;
  }

}
