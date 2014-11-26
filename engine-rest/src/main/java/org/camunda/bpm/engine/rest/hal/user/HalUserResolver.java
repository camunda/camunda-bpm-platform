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
package org.camunda.bpm.engine.rest.hal.user;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.cache.HalIdResourceCacheLinkResolver;

/**
 * @author Daniel Meyer
 *
 */
public class HalUserResolver extends HalIdResourceCacheLinkResolver {

  protected Class<?> getHalResourceClass() {
    return HalUser.class;
  }

  protected List<HalResource<?>> resolveNotCachedLinks(String[] linkedIds, ProcessEngine processEngine) {
    IdentityService identityService = processEngine.getIdentityService();

    List<User> users = identityService.createUserQuery()
      .userIdIn(linkedIds)
      .listPage(0, linkedIds.length);

    List<HalResource<?>> resolvedUsers = new ArrayList<HalResource<?>>();
    for (User user : users) {
      resolvedUsers.add(HalUser.fromUser(user));
    }

    return resolvedUsers;
  }

}
