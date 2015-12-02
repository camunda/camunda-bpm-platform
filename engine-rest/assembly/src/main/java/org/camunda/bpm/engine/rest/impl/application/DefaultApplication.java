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
package org.camunda.bpm.engine.rest.impl.application;

import org.camunda.bpm.engine.rest.impl.CamundaRestResources;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Default {@link Application} registering all resources.</p>
 *
 * <p><strong>NOTE</strong> This class is excluded from the classes-jar,
 * such that users that want to embed the REST API as a JAR file into a
 * custom JAX-RS application are able to build a deployment based on their
 * requirements.</p>
 *
 * @author Daniel Meyer
 *
 */
@ApplicationPath("/")
public class DefaultApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.addAll(CamundaRestResources.getResourceClasses());
    classes.addAll(CamundaRestResources.getConfigurationClasses());

    return classes;
  }

}
