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
package org.camunda.bpm.identity.impl.ldap;

import org.camunda.bpm.engine.identity.User;

import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
public class LdapUserQueryTest extends LdapIdentityProviderTest {

  public void testQueryNoFilter() {
    List<User> result = identityService.createUserQuery().list();
    assertEquals(8, result.size());
  }

  public void testFilterByUserId() {
    User user = identityService.createUserQuery().userId("oscar").singleResult();
    assertNotNull(user);

    // validate user
    assertEquals("oscar", user.getId());
    assertEquals("Oscar", user.getFirstName());
    assertEquals("The Crouch", user.getLastName());
    assertEquals("oscar@camunda.org", user.getEmail());


    user = identityService.createUserQuery().userId("non-existing").singleResult();
    assertNull(user);
  }

  public void testFilterByUserIdIn() {
    List<User> users = identityService.createUserQuery().userIdIn("oscar", "monster").list();
    assertNotNull(users);
    assertEquals(2, users.size());

    users = identityService.createUserQuery().userIdIn("oscar", "monster", "daniel").list();
    assertNotNull(users);
    assertEquals(3, users.size());

    users = identityService.createUserQuery().userIdIn("oscar", "monster", "daniel", "non-existing").list();
    assertNotNull(users);
    assertEquals(3, users.size());
  }

  public void testFilterByFirstname() {
    User user = identityService.createUserQuery().userFirstName("Oscar").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userFirstName("non-existing").singleResult();
    assertNull(user);
  }

  public void testFilterByFirstnameLike() {
    User user = identityService.createUserQuery().userFirstNameLike("Osc*").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userFirstNameLike("non-exist*").singleResult();
    assertNull(user);
  }

  public void testFilterByLastname() {
    User user = identityService.createUserQuery().userLastName("The Crouch").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userFirstNameLike("non-existing").singleResult();
    assertNull(user);
  }

  public void testFilterByLastnameLike() {
    User user = identityService.createUserQuery().userLastNameLike("The Cro*").singleResult();
    assertNotNull(user);
    user = identityService.createUserQuery().userLastNameLike("The*").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userLastNameLike("non-exist*").singleResult();
    assertNull(user);
  }

  public void testFilterByEmail() {
    User user = identityService.createUserQuery().userEmail("oscar@camunda.org").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userEmail("non-exist*").singleResult();
    assertNull(user);
  }

  public void testFilterByEmailLike() {
    User user = identityService.createUserQuery().userEmailLike("oscar@*").singleResult();
    assertNotNull(user);

    user = identityService.createUserQuery().userEmailLike("non-exist*").singleResult();
    assertNull(user);
  }

  public void testFilterByGroupId() {
    List<User> result = identityService.createUserQuery().memberOfGroup("development").list();
    assertEquals(3, result.size());
  }

  public void testFilterByGroupIdAndFirstname() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userFirstName("Oscar")
        .list();
    assertEquals(1, result.size());
  }

  public void testFilterByGroupIdAndId() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userId("oscar")
        .list();
    assertEquals(1, result.size());
  }

  public void testFilterByGroupIdAndLastname() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userLastName("The Crouch")
        .list();
    assertEquals(1, result.size());
  }

  public void testFilterByGroupIdAndEmail() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userEmail("oscar@camunda.org")
        .list();
    assertEquals(1, result.size());
  }

  public void testFilterByGroupIdAndEmailLike() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("development")
        .userEmail("*@camunda.org")
        .list();
    assertEquals(3, result.size());
  }

  public void testFilterByGroupIdAndIdForDnUsingCn() {
    List<User> result = identityService.createUserQuery()
        .memberOfGroup("external")
        .userId("fozzie")
        .list();
    assertEquals(1, result.size());
  }

  public void testAuthenticatedUserSeesHimself() {
    try {
      processEngineConfiguration.setAuthorizationEnabled(true);

      identityService.setAuthenticatedUserId("non-existing");
      assertEquals(0, identityService.createUserQuery().count());

      identityService.setAuthenticatedUserId("oscar");
      assertEquals(1, identityService.createUserQuery().count());

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
      identityService.clearAuthentication();
    }
  }

}
