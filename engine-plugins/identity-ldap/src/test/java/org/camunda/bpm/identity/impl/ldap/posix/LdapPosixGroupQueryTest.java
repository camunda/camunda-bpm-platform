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
package org.camunda.bpm.identity.impl.ldap.posix;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;

import java.util.List;


/**
 * @author Tom Crossland
 */
public class LdapPosixGroupQueryTest extends LdapPosixTest {

  public void testFilterByGroupId() {
    Group group = identityService.createGroupQuery().groupId("posix-group-without-members").singleResult();
    assertNotNull(group);

    group = identityService.createGroupQuery().groupId("posix-group-with-members").singleResult();
    assertNotNull(group);

    List<User> result = identityService.createUserQuery().memberOfGroup("posix-group-without-members").list();
    assertEquals(0, result.size());

    result = identityService.createUserQuery().memberOfGroup("posix-group-with-members").list();
    assertEquals(3, result.size());
  }

}
