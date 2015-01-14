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
package org.camunda.bpm.tasklist.test.sample.simple;

import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.tasklist.plugin.spi.impl.AbstractTasklistPlugin;
import org.camunda.bpm.tasklist.test.sample.simple.resources.SimpleTasklistRootResource;

/**
 * @author Roman Smirnov
 *
 */
public class SimpleTasklistPlugin extends AbstractTasklistPlugin {

  public static final String ID = "simple";

  public String getId() {
    return ID;
  }

  public Set<Class<?>> getResourceClasses() {
    final HashSet<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(SimpleTasklistRootResource.class);

    return classes;
  }

}
