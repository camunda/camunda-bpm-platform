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

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.hal.HalIdResource;
import org.camunda.bpm.engine.rest.hal.HalRelation;
import org.camunda.bpm.engine.rest.hal.HalResource;

import javax.ws.rs.core.UriBuilder;

/**
 * @author Daniel Meyer
 *
 */
public class HalUser extends HalResource<HalUser> implements HalIdResource {

  public final static HalRelation REL_SELF =
    HalRelation.build("self", UserRestService.class, UriBuilder.fromPath(UserRestService.PATH).path("{id}"));

  protected String id;
  protected String firstName;
  protected String lastName;
  protected String email;


  public static HalUser fromUser(User user) {

    HalUser halUser = new HalUser();

    halUser.id = user.getId();
    halUser.firstName = user.getFirstName();
    halUser.lastName = user.getLastName();
    halUser.email = user.getEmail();

    halUser.linker.createLink(REL_SELF, user.getId());

    return halUser;

  }

  public String getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

}
