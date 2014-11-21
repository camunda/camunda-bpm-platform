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

import javax.ws.rs.core.UriBuilder;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.rest.GroupRestService;
import org.camunda.bpm.engine.rest.hal.HalIdResource;
import org.camunda.bpm.engine.rest.hal.HalRelation;
import org.camunda.bpm.engine.rest.hal.HalResource;

public class HalGroup extends HalResource<HalGroup> implements HalIdResource {

  public final static HalRelation REL_SELF =
    HalRelation.build("self", GroupRestService.class, UriBuilder.fromPath(GroupRestService.PATH).path("{id}"));

  protected String id;
  protected String name;
  protected String type;

  public static HalGroup fromGroup(Group group) {
    HalGroup halGroup = new HalGroup();

    halGroup.id = group.getId();
    halGroup.name = group.getName();
    halGroup.type = group.getType();

    halGroup.linker.createLink(REL_SELF, group.getId());

    return halGroup;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

}
