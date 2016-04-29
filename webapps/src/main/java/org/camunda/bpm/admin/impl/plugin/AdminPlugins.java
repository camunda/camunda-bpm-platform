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
package org.camunda.bpm.admin.impl.plugin;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.admin.impl.plugin.resources.AdminPluginsRootResource;
import org.camunda.bpm.admin.plugin.spi.impl.AbstractAdminPlugin;

/**
 *
 * @author vale
 */
public class AdminPlugins extends AbstractAdminPlugin {

  public static final String ID = "adminPlugins";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public Set<Class<?>> getResourceClasses() {
    HashSet<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(AdminPluginsRootResource.class);

    return classes;
  }

  @Override
  public String getAssetDirectory() {
    return "plugin/admin";
  }

}
