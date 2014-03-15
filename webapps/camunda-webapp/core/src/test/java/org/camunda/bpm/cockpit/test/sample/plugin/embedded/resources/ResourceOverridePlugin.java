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
package org.camunda.bpm.cockpit.test.sample.plugin.embedded.resources;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.cockpit.test.sample.plugin.embedded.EmbeddedPlugin;
import org.camunda.bpm.webapp.plugin.resource.PluginResourceOverride;

/**
 * @author Daniel Meyer
 *
 */
public class ResourceOverridePlugin extends EmbeddedPlugin {

  @Override
  public List<PluginResourceOverride> getResourceOverrides() {

    ArrayList<PluginResourceOverride> resourceOverrides = new ArrayList<PluginResourceOverride>();

    resourceOverrides.add(new ExampleResourceOverride());

    return resourceOverrides;

  }

}
