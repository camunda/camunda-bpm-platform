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
package org.camunda.bpm.cockpit.impl.plugin.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.cockpit.impl.plugin.base.resources.BaseRootResource;
import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;

/**
 *
 * @author nico.rehwaldt
 */
public class BasePlugin extends AbstractCockpitPlugin {

  public static final String ID = "base";

  private static final String[] MAPPING_FILES = {
    "org/camunda/bpm/cockpit/plugin/base/queries/processDefinition.xml",
    "org/camunda/bpm/cockpit/plugin/base/queries/processInstance.xml",
    "org/camunda/bpm/cockpit/plugin/base/queries/incident.xml"
  };

  @Override
  public Set<Class<?>> getResourceClasses() {
    HashSet<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(BaseRootResource.class);

    return classes;
  }

  public String getAssetDirectory() {
    return "plugin/base";
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public List<String> getMappingFiles() {
    return Arrays.asList(MAPPING_FILES);
  }
}
