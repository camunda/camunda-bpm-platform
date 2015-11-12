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
package org.camunda.bpm.engine.rest.hal.group;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.cache.HalIdResourceCacheLinkResolver;

public class HalGroupResolver extends HalIdResourceCacheLinkResolver {

  protected Class<?> getHalResourceClass() {
    return HalGroup.class;
  }

  protected List<HalResource<?>> resolveNotCachedLinks(String[] linkedIds, ProcessEngine processEngine) {
    IdentityService identityService = processEngine.getIdentityService();

    List<Group> groups = identityService.createGroupQuery()
      .groupIdIn(linkedIds)
      .listPage(0, linkedIds.length);

    List<HalResource<?>> resolvedGroups = new ArrayList<HalResource<?>>();
    for (Group group : groups) {
      resolvedGroups.add(HalGroup.fromGroup(group));
    }

    return resolvedGroups;
  }

}
