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
package org.camunda.bpm.container.impl.jmx.services;

import java.util.List;

import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugin;
import org.camunda.bpm.container.impl.plugin.BpmPlatformPlugins;
import org.camunda.bpm.container.impl.spi.PlatformService;
import org.camunda.bpm.container.impl.spi.PlatformServiceContainer;

/**
 * @author Thorben Lindhauer
 *
 */
public class JmxManagedBpmPlatformPlugins implements PlatformService<BpmPlatformPlugins>, JmxManagedBpmPlatformPluginsMBean {

  protected BpmPlatformPlugins plugins;

  public JmxManagedBpmPlatformPlugins(BpmPlatformPlugins plugins) {
    this.plugins = plugins;
  }

  @Override
  public void start(PlatformServiceContainer mBeanServiceContainer) {
    // no callbacks or initialization in the plugins
  }

  @Override
  public void stop(PlatformServiceContainer mBeanServiceContainer) {
    // no callbacks or initialization in the plugins
  }

  @Override
  public BpmPlatformPlugins getValue() {
    return plugins;
  }

  @Override
  public String[] getPluginNames() {
    // expose names of discovered plugins in JMX
    List<BpmPlatformPlugin> pluginList = plugins.getPlugins();
    String[] names = new String[pluginList.size()];
    for (int i = 0; i < names.length; i++) {
      BpmPlatformPlugin bpmPlatformPlugin = pluginList.get(i);
      if(bpmPlatformPlugin != null) {
        names[i] = bpmPlatformPlugin.getClass().getName();
      }
    }
    return names;
  }

}
