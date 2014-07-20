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
package org.camunda.bpm.engine.rest.hal;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.hal.processDefinition.HalProcessDefinitionResolver;
import org.camunda.bpm.engine.rest.hal.user.HalUserResolver;

/**
 * @author Daniel Meyer
 *
 */
public class Hal {

  public static final String MEDIA_TYPE_HAL = "application/hal+json";

  public static Hal instance = new Hal();

  protected Map<Class<?>, HalLinkResolver> halLinkResolvers = new HashMap<Class<?>, HalLinkResolver>();

  public Hal() {
    // register the built-in resolvers
    halLinkResolvers.put(UserRestService.class, new HalUserResolver());
    halLinkResolvers.put(ProcessDefinitionRestService.class, new HalProcessDefinitionResolver());
  }

  public static Hal getInstance() {
    return instance;
  }

  public HalLinker createLinker(HalResource<?> resource) {
    return new HalLinker(this, resource);
  }

  public HalLinkResolver getLinkResolver(Class<?> resourceClass) {
    return halLinkResolvers.get(resourceClass);
  }

}
