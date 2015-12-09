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
package org.camunda.bpm.container.impl.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Thorben Lindhauer
 *
 */
public class BpmPlatformPlugins {

  protected List<BpmPlatformPlugin> plugins;

  public BpmPlatformPlugins() {
    this.plugins = new ArrayList<BpmPlatformPlugin>();
  }

  public void add(BpmPlatformPlugin plugin) {
    this.plugins.add(plugin);
  }

  public List<BpmPlatformPlugin> getPlugins() {
    return plugins;
  }

  public static BpmPlatformPlugins load(ClassLoader classLoader) {
    BpmPlatformPlugins plugins = new BpmPlatformPlugins();

    Iterator<BpmPlatformPlugin> it = ServiceLoader
        .load(BpmPlatformPlugin.class, classLoader)
        .iterator();

    while (it.hasNext()) {
      plugins.add(it.next());
    }

    return plugins;
  }
}
