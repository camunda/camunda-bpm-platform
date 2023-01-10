/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Picture;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.Account;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Frederik Heremans
 */
public class IdentityServiceTest {

  private final String INVALID_ID_MESSAGE = "%s has an invalid id: '%s' is not a valid resource identifier.";

  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static final String INDENTITY_LOGGER = "org.camunda.bpm.engine.identity";

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  protected IdentityService identityService;
  protected ProcessEngine processEngine;

  @Before
  public void init() {
    identityService = engineRule.getIdentityService();
  }

  @After
  public void cleanUp() {
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    ClockUtil.setCurrentTime(new Date());

    if (processEngine != null) {

      for (User user : processEngine.getIdentityService().createUserQuery().list()) {
        processEngine.getIdentityService().deleteUser(user.getId());
      }
      for (Group group : processEngine.getIdentityService().createGroupQuery().list()) {
        processEngine.getIdentityService().deleteGroup(group.getId());
      }
      for (Tenant tenant : processEngine.getIdentityService().createTenantQuery().list()) {
        processEngine.getIdentityService().deleteTenant(tenant.getId());
      }
      for (Authorization authorization : processEngine.getAuthorizationService().createAuthorizationQuery().list()) {
        processEngine.getAuthorizationService().deleteAuthorization(authorization.getId());
      }

      processEngine.close();
      ProcessEngines.unregister(processEngine);
      processEngine = null;
    }
  }

  @Test
  public void testIsReadOnly() {
    assertFalse(identityService.isReadOnly());
  }

  @Test
  public void testUserInfo() {
    User user = identityService.newUser("testuser");
    identityService.saveUser(user);

    identityService.setUserInfo("testuser", "myinfo", "myvalue");
    assertEquals("myvalue", identityService.getUserInfo("testuser", "myinfo"));

    identityService.setUserInfo("testuser", "myinfo", "myvalue2");
    assertEquals("myvalue2", identityService.getUserInfo("testuser", "myinfo"));

    identityService.deleteUserInfo("testuser", "myinfo");
    assertNull(identityService.getUserInfo("testuser", "myinfo"));

    identityService.deleteUser(user.getId());
  }

  @Test
  public void testUserAccount() {
    User user = identityService.newUser("testuser");
    identityService.saveUser(user);

    identityService.setUserAccount("testuser", "123", "google", "mygoogleusername", "mygooglepwd", null);
    Account googleAccount = identityService.getUserAccount("testuser", "123", "google");
    assertEquals("google", googleAccount.getName());
    assertEquals("mygoogleusername", googleAccount.getUsername());
    assertEquals("mygooglepwd", googleAccount.getPassword());

    identityService.setUserAccount("testuser", "123", "google", "mygoogleusername2", "mygooglepwd2", null);
    googleAccount = identityService.getUserAccount("testuser", "123", "google");
    assertEquals("google", googleAccount.getName());
    assertEquals("mygoogleusername2", googleAccount.getUsername());
    assertEquals("mygooglepwd2", googleAccount.getPassword());

    identityService.setUserAccount("testuser", "123", "alfresco", "myalfrescousername", "myalfrescopwd", null);
    identityService.setUserInfo("testuser", "myinfo", "myvalue");
    identityService.setUserInfo("testuser", "myinfo2", "myvalue2");

    List<String> expectedUserAccountNames = new ArrayList<String>();
    expectedUserAccountNames.add("google");
    expectedUserAccountNames.add("alfresco");
    List<String> userAccountNames = identityService.getUserAccountNames("testuser");
    assertListElementsMatch(expectedUserAccountNames, userAccountNames);

    identityService.deleteUserAccount("testuser", "google");

    expectedUserAccountNames.remove("google");

    userAccountNames = identityService.getUserAccountNames("testuser");
    assertListElementsMatch(expectedUserAccountNames, userAccountNames);

    identityService.deleteUser(user.getId());
  }

  private void assertListElementsMatch(List<String> list1, List<String> list2) {
    if (list1 != null) {
      assertNotNull(list2);
      assertEquals(list1.size(), list2.size());
      for (String value : list1) {
        assertTrue(list2.contains(value));
      }
    } else {
      assertNull(list2);
    }

  }

  @Test
  public void testUserAccountDetails() {
    User user = identityService.newUser("testuser");
    identityService.saveUser(user);

    Map<String, String> accountDetails = new HashMap<String, String>();
    accountDetails.put("server", "localhost");
    accountDetails.put("port", "35");
    identityService.setUserAccount("testuser", "123", "google", "mygoogleusername", "mygooglepwd", accountDetails);
    Account googleAccount = identityService.getUserAccount("testuser", "123", "google");
    assertEquals(accountDetails, googleAccount.getDetails());

    identityService.deleteUser(user.getId());
  }

  @Test
  public void testCreateExistingUser() {
    User user = identityService.newUser("testuser");
    identityService.saveUser(user);

    User secondUser = identityService.newUser("testuser");

    try {
      identityService.saveUser(secondUser);
      fail("BadUserRequestException is expected");
    } catch (Exception ex) {
      if (!(ex instanceof BadUserRequestException)) {
        fail("BadUserRequestException is expected, but another exception was received:  " + ex);
      }
      assertEquals("The user already exists", ex.getMessage());
    }
  }

  @Test
  public void testUpdateUser() {
    // First, create a new user
    User user = identityService.newUser("johndoe");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("johndoe@alfresco.com");
    user.setPassword("s3cret");
    identityService.saveUser(user);

    // Fetch and update the user
    user = identityService.createUserQuery().userId("johndoe").singleResult();
    user.setEmail("updated@alfresco.com");
    user.setFirstName("Jane");
    user.setLastName("Donnel");
    identityService.saveUser(user);

    user = identityService.createUserQuery().userId("johndoe").singleResult();
    assertEquals("Jane", user.getFirstName());
    assertEquals("Donnel", user.getLastName());
    assertEquals("updated@alfresco.com", user.getEmail());
    assertTrue(identityService.checkPassword("johndoe", "s3cret"));

    identityService.deleteUser(user.getId());
  }

  @Test
  public void testUserPicture() {
    // First, create a new user
    User user = identityService.newUser("johndoe");
    identityService.saveUser(user);
    String userId = user.getId();

    Picture picture = new Picture("niceface".getBytes(), "image/string");
    identityService.setUserPicture(userId, picture);

    picture = identityService.getUserPicture(userId);

    // Fetch and update the user
    user = identityService.createUserQuery().userId("johndoe").singleResult();
    assertTrue("byte arrays differ", Arrays.equals("niceface".getBytes(), picture.getBytes()));
    assertEquals("image/string", picture.getMimeType());

    identityService.deleteUserPicture("johndoe");
    // this is ignored
    identityService.deleteUserPicture("someone-else-we-dont-know");

    // picture does not exist
    picture = identityService.getUserPicture("johndoe");
    assertNull(picture);

    // add new picture
    picture = new Picture("niceface".getBytes(), "image/string");
    identityService.setUserPicture(userId, picture);

    // makes the picture go away
    identityService.deleteUser(user.getId());
  }

  @Test
  public void testCreateExistingGroup() {
    Group group = identityService.newGroup("greatGroup");
    identityService.saveGroup(group);

    Group secondGroup = identityService.newGroup("greatGroup");

    try {
      identityService.saveGroup(secondGroup);
      fail("BadUserRequestException is expected");
    } catch (Exception ex) {
      if (!(ex instanceof BadUserRequestException)) {
        fail("BadUserRequestException is expected, but another exception was received:  " + ex);
      }
      assertEquals("The group already exists", ex.getMessage());
    }
  }

  @Test
  public void testUpdateGroup() {
    Group group = identityService.newGroup("sales");
    group.setName("Sales");
    identityService.saveGroup(group);

    group = identityService.createGroupQuery().groupId("sales").singleResult();
    group.setName("Updated");
    identityService.saveGroup(group);

    group = identityService.createGroupQuery().groupId("sales").singleResult();
    assertEquals("Updated", group.getName());

    identityService.deleteGroup(group.getId());
  }

  @Test
  public void findUserByUnexistingId() {
    User user = identityService.createUserQuery().userId("unexistinguser").singleResult();
    assertNull(user);
  }

  @Test
  public void findGroupByUnexistingId() {
    Group group = identityService.createGroupQuery().groupId("unexistinggroup").singleResult();
    assertNull(group);
  }

  @Test
  public void testCreateMembershipUnexistingGroup() {
    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);

    // when/then
    assertThatThrownBy(() -> identityService.createMembership(johndoe.getId(), "unexistinggroup"))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("No group found with id 'unexistinggroup'.: group is null");
  }

  @Test
  public void testCreateMembershipUnexistingUser() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    // when/then
    assertThatThrownBy(() -> identityService.createMembership("unexistinguser", sales.getId()))
      .isInstanceOf(NullValueException.class)
      .hasMessageContaining("No user found with id 'unexistinguser'.: user is null");
  }

  @Test
  public void testCreateMembershipAlreadyExisting() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);
    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);

    // Create the membership
    identityService.createMembership(johndoe.getId(), sales.getId());

    // when/then
    assertThatThrownBy(() -> identityService.createMembership(johndoe.getId(), sales.getId()))
      .isInstanceOf(ProcessEngineException.class);
  }

  @Test
  public void testSaveGroupNullArgument() {
    // when/then
    assertThatThrownBy(() -> identityService.saveGroup(null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("group is null");
  }

  @Test
  public void testSaveUserNullArgument() {
    // when/then
    assertThatThrownBy(() -> identityService.saveUser(null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("user is null");
  }

  @Test
  public void testFindGroupByIdNullArgument() {
    // when/then
    assertThatThrownBy(() -> identityService.createGroupQuery().groupId(null).singleResult())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("id is null");
  }

  @Test
  public void testCreateMembershipNullUserArgument() {
    // when/then
    assertThatThrownBy(() -> identityService.createMembership(null, "group"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("userId is null");
  }

  @Test
  public void testCreateMembershipNullGroupArgument() {
    // when/then
    assertThatThrownBy(() -> identityService.createMembership("userId", null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("groupId is null");
  }

  @Test
  public void testFindGroupsByUserIdNullArguments() {
    // when/then
    assertThatThrownBy(() -> identityService.createGroupQuery().groupMember(null).singleResult())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("userId is null");
  }

  @Test
  public void testFindUsersByGroupUnexistingGroup() {
    List<User> users = identityService.createUserQuery().memberOfGroup("unexistinggroup").list();
    assertNotNull(users);
    assertTrue(users.isEmpty());
  }

  @Test
  public void testDeleteGroupNullArguments() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteGroup(null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("groupId is null");
  }

  @Test
  public void testDeleteMembership() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);
    // Add membership
    identityService.createMembership(johndoe.getId(), sales.getId());

    List<Group> groups = identityService.createGroupQuery().groupMember(johndoe.getId()).list();
    assertTrue(groups.size() == 1);
    assertEquals("sales", groups.get(0).getId());

    // Delete the membership and check members of sales group
    identityService.deleteMembership(johndoe.getId(), sales.getId());
    groups = identityService.createGroupQuery().groupMember(johndoe.getId()).list();
    assertTrue(groups.size() == 0);

    identityService.deleteGroup("sales");
    identityService.deleteUser("johndoe");
  }

  @Test
  public void testDeleteMembershipWhenUserIsNoMember() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);

    // Delete the membership when the user is no member
    identityService.deleteMembership(johndoe.getId(), sales.getId());

    identityService.deleteGroup("sales");
    identityService.deleteUser("johndoe");
  }

  @Test
  public void testDeleteMembershipUnexistingGroup() {
    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);
    // No exception should be thrown when group doesn't exist
    identityService.deleteMembership(johndoe.getId(), "unexistinggroup");
    identityService.deleteUser(johndoe.getId());
  }

  @Test
  public void testDeleteMembershipUnexistingUser() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);
    // No exception should be thrown when user doesn't exist
    identityService.deleteMembership("unexistinguser", sales.getId());
    identityService.deleteGroup(sales.getId());
  }

  @Test
  public void testDeleteMemberschipNullUserArgument() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteMembership(null, "group"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("userId is null");
  }

  @Test
  public void testDeleteMemberschipNullGroupArgument() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteMembership("user", null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("groupId is null");
  }

  @Test
  public void testDeleteUserNullArguments() {
    // when/then
    assertThatThrownBy(() -> identityService.deleteUser(null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("userId is null");
  }

  @Test
  public void testDeleteUserUnexistingUserId() {
    // No exception should be thrown. Deleting an unexisting user should
    // be ignored silently
    identityService.deleteUser("unexistinguser");
  }

  @Test
  public void testCheckPassword() {

    // store user with password
    User user = identityService.newUser("secureUser");
    user.setPassword("s3cret");
    identityService.saveUser(user);

    assertTrue(identityService.checkPassword(user.getId(), "s3cret"));
    assertFalse(identityService.checkPassword(user.getId(), "wrong"));

    identityService.deleteUser(user.getId());

  }

  @Test
  public void testUpdatePassword() {

    // store user with password
    User user = identityService.newUser("secureUser");
    user.setPassword("s3cret");
    identityService.saveUser(user);

    assertTrue(identityService.checkPassword(user.getId(), "s3cret"));

    user.setPassword("new-password");
    identityService.saveUser(user);

    assertTrue(identityService.checkPassword(user.getId(), "new-password"));

    identityService.deleteUser(user.getId());

  }

  @Test
  public void testCheckPasswordNullSafe() {
    assertFalse(identityService.checkPassword("userId", null));
    assertFalse(identityService.checkPassword(null, "passwd"));
    assertFalse(identityService.checkPassword(null, null));
  }

  @Test
  public void testUserOptimisticLockingException() {
    User user = identityService.newUser("kermit");
    identityService.saveUser(user);

    User user1 = identityService.createUserQuery().singleResult();
    User user2 = identityService.createUserQuery().singleResult();

    user1.setFirstName("name one");
    identityService.saveUser(user1);

    user2.setFirstName("name two");

    // when/then
    assertThatThrownBy(() -> identityService.saveUser(user2))
      .isInstanceOf(OptimisticLockingException.class);
  }

  @Test
  public void testGroupOptimisticLockingException() {
    Group group = identityService.newGroup("group");
    identityService.saveGroup(group);

    Group group1 = identityService.createGroupQuery().singleResult();
    Group group2 = identityService.createGroupQuery().singleResult();

    group1.setName("name one");
    identityService.saveGroup(group1);

    group2.setName("name two");

    // when/then
    assertThatThrownBy(() -> identityService.saveGroup(group2))
      .isInstanceOf(OptimisticLockingException.class);
  }

  @Test
  public void testSaveUserWithGenericResourceId() {
    processEngine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/api/identity/generic.resource.id.whitelist.camunda.cfg.xml")
      .buildProcessEngine();

    User user = processEngine.getIdentityService().newUser("*");

    // when/then
    assertThatThrownBy(() -> processEngine.getIdentityService().saveUser(user))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("has an invalid id: id cannot be *. * is a reserved identifier.");
  }

  @Test
  public void testSaveGroupWithGenericResourceId() {
    processEngine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/api/identity/generic.resource.id.whitelist.camunda.cfg.xml")
      .buildProcessEngine();

    Group group = processEngine.getIdentityService().newGroup("*");

    // when/then
    assertThatThrownBy(() -> processEngine.getIdentityService().saveGroup(group))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("has an invalid id: id cannot be *. * is a reserved identifier.");
  }

  @Test
  public void testSetAuthenticatedIdToGenericId() {

    // when/then
    assertThatThrownBy(() -> identityService.setAuthenticatedUserId("*"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Invalid user id provided: id cannot be *. * is a reserved identifier.");
  }

  @Test
  public void testSetAuthenticationUserIdToGenericId() {
    // when/then
    assertThatThrownBy(() -> identityService.setAuthentication("aUserId", Arrays.asList("*")))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("invalid group id provided: id cannot be *. * is a reserved identifier.");
  }

  @Test
  public void testSetAuthenticatedTenantIdToGenericId() {
    // when/then
    assertThatThrownBy(() -> identityService.setAuthentication(null, null, Arrays.asList("*")))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("invalid tenant id provided: id cannot be *. * is a reserved identifier.");
  }

  @Test
  public void testSetAuthenticatedUserId() {
    identityService.setAuthenticatedUserId("john");

    Authentication currentAuthentication = identityService.getCurrentAuthentication();

    assertNotNull(currentAuthentication);
    assertEquals("john", currentAuthentication.getUserId());
    assertNull(currentAuthentication.getGroupIds());
    assertNull(currentAuthentication.getTenantIds());
  }

  @Test
  public void testSetAuthenticatedUserAndGroups() {
    List<String> groups = Arrays.asList("sales", "development");

    identityService.setAuthentication("john", groups);

    Authentication currentAuthentication = identityService.getCurrentAuthentication();

    assertNotNull(currentAuthentication);
    assertEquals("john", currentAuthentication.getUserId());
    assertEquals(groups, currentAuthentication.getGroupIds());
    assertNull(currentAuthentication.getTenantIds());
  }

  @Test
  public void testSetAuthenticatedUserGroupsAndTenants() {
    List<String> groups = Arrays.asList("sales", "development");
    List<String> tenants = Arrays.asList("tenant1", "tenant2");

    identityService.setAuthentication("john", groups, tenants);

    Authentication currentAuthentication = identityService.getCurrentAuthentication();

    assertNotNull(currentAuthentication);
    assertEquals("john", currentAuthentication.getUserId());
    assertEquals(groups, currentAuthentication.getGroupIds());
    assertEquals(tenants, currentAuthentication.getTenantIds());
  }

  @Test
  public void testAuthentication() {
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    assertTrue(identityService.checkPassword("johndoe", "xxx"));
    assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));

    identityService.deleteUser("johndoe");
  }

  @Test
  @WatchLogger(loggerNames = {INDENTITY_LOGGER}, level = "INFO")
  public void testUsuccessfulAttemptsResultInBlockedUser() throws ParseException {
    // given
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    Date now = sdf.parse("2000-01-24T13:00:00");
    ClockUtil.setCurrentTime(now);

    // when
    for (int i = 0; i < 11; i++) {
      assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));
      now = DateUtils.addMinutes(now, 1);
      ClockUtil.setCurrentTime(now);
    }

    // then
    assertThat(loggingRule.getFilteredLog(INDENTITY_LOGGER, "The user with id 'johndoe' is permanently locked.").size()).isEqualTo(1);
  }

  @Test
  public void testSuccessfulLoginAfterFailureAndDelay() {
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    Date now = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(now);
    assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));
    ClockUtil.setCurrentTime(DateUtils.addSeconds(now, 30));
    assertTrue(identityService.checkPassword("johndoe", "xxx"));

    identityService.deleteUser("johndoe");
  }

  @Test
  @WatchLogger(loggerNames = {INDENTITY_LOGGER}, level = "INFO")
  public void testSuccessfulLoginAfterFailureWithoutDelay() {
    // given
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    Date now = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(now);
    assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));
    assertFalse(identityService.checkPassword("johndoe", "xxx"));

    // assume
    assertThat(loggingRule.getFilteredLog(INDENTITY_LOGGER, "The user with id 'johndoe' is locked.").size()).isEqualTo(1);

    // when
    ClockUtil.setCurrentTime(DateUtils.addSeconds(now, 30));
    boolean checkPassword = identityService.checkPassword("johndoe", "xxx");

    // then
    assertTrue(checkPassword);
  }

  @Test
  @WatchLogger(loggerNames = {INDENTITY_LOGGER}, level = "INFO")
  public void testUnsuccessfulLoginAfterFailureWithoutDelay() {
    // given
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    Date now = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(now);
    assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));

    ClockUtil.setCurrentTime(DateUtils.addSeconds(now, 1));
    Date expectedLockExpitation = DateUtils.addSeconds(now, 3);

    // when try again before exprTime
    assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));

    // then
    assertThat(loggingRule.getFilteredLog(INDENTITY_LOGGER, "The lock will expire at " + expectedLockExpitation).size()).isEqualTo(1);
  }

  @Test
  public void testFindGroupsByUserAndType() {
    Group sales = identityService.newGroup("sales");
    sales.setType("hierarchy");
    identityService.saveGroup(sales);

    Group development = identityService.newGroup("development");
    development.setType("hierarchy");
    identityService.saveGroup(development);

    Group admin = identityService.newGroup("admin");
    admin.setType("security-role");
    identityService.saveGroup(admin);

    Group user = identityService.newGroup("user");
    user.setType("security-role");
    identityService.saveGroup(user);

    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);

    User joesmoe = identityService.newUser("joesmoe");
    identityService.saveUser(joesmoe);

    User jackblack = identityService.newUser("jackblack");
    identityService.saveUser(jackblack);

    identityService.createMembership("johndoe", "sales");
    identityService.createMembership("johndoe", "user");
    identityService.createMembership("johndoe", "admin");

    identityService.createMembership("joesmoe", "user");

    List<Group> groups = identityService.createGroupQuery().groupMember("johndoe").groupType("security-role").list();
    Set<String> groupIds = getGroupIds(groups);
    Set<String> expectedGroupIds = new HashSet<String>();
    expectedGroupIds.add("user");
    expectedGroupIds.add("admin");
    assertEquals(expectedGroupIds, groupIds);

    groups = identityService.createGroupQuery().groupMember("joesmoe").groupType("security-role").list();
    groupIds = getGroupIds(groups);
    expectedGroupIds = new HashSet<String>();
    expectedGroupIds.add("user");
    assertEquals(expectedGroupIds, groupIds);

    groups = identityService.createGroupQuery().groupMember("jackblack").groupType("security-role").list();
    assertTrue(groups.isEmpty());

    identityService.deleteGroup("sales");
    identityService.deleteGroup("development");
    identityService.deleteGroup("admin");
    identityService.deleteGroup("user");
    identityService.deleteUser("johndoe");
    identityService.deleteUser("joesmoe");
    identityService.deleteUser("jackblack");
  }

  @Test
  public void testUser() {
    User user = identityService.newUser("johndoe");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("johndoe@alfresco.com");
    identityService.saveUser(user);

    user = identityService.createUserQuery().userId("johndoe").singleResult();
    assertEquals("johndoe", user.getId());
    assertEquals("John", user.getFirstName());
    assertEquals("Doe", user.getLastName());
    assertEquals("johndoe@alfresco.com", user.getEmail());

    identityService.deleteUser("johndoe");
  }

  @Test
  public void testGroup() {
    Group group = identityService.newGroup("sales");
    group.setName("Sales division");
    identityService.saveGroup(group);

    group = identityService.createGroupQuery().groupId("sales").singleResult();
    assertEquals("sales", group.getId());
    assertEquals("Sales division", group.getName());

    identityService.deleteGroup("sales");
  }

  @Test
  public void testMembership() {
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    Group development = identityService.newGroup("development");
    identityService.saveGroup(development);

    User johndoe = identityService.newUser("johndoe");
    identityService.saveUser(johndoe);

    User joesmoe = identityService.newUser("joesmoe");
    identityService.saveUser(joesmoe);

    User jackblack = identityService.newUser("jackblack");
    identityService.saveUser(jackblack);

    identityService.createMembership("johndoe", "sales");
    identityService.createMembership("joesmoe", "sales");

    identityService.createMembership("joesmoe", "development");
    identityService.createMembership("jackblack", "development");

    List<Group> groups = identityService.createGroupQuery().groupMember("johndoe").list();
    assertEquals(createStringSet("sales"), getGroupIds(groups));

    groups = identityService.createGroupQuery().groupMember("joesmoe").list();
    assertEquals(createStringSet("sales", "development"), getGroupIds(groups));

    groups = identityService.createGroupQuery().groupMember("jackblack").list();
    assertEquals(createStringSet("development"), getGroupIds(groups));

    List<User> users = identityService.createUserQuery().memberOfGroup("sales").list();
    assertEquals(createStringSet("johndoe", "joesmoe"), getUserIds(users));

    users = identityService.createUserQuery().memberOfGroup("development").list();
    assertEquals(createStringSet("joesmoe", "jackblack"), getUserIds(users));

    identityService.deleteGroup("sales");
    identityService.deleteGroup("development");

    identityService.deleteUser("jackblack");
    identityService.deleteUser("joesmoe");
    identityService.deleteUser("johndoe");
  }

  @Test
  public void testInvalidUserId() {
    String invalidId = "john doe";
    User user = identityService.newUser(invalidId);

    try {
      identityService.saveUser(user);
      fail("Invalid user id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "User", invalidId), ex.getMessage());
    }
  }

  @Test
  public void testInvalidUserIdOnSave() {
    String invalidId = "john doe";
    try {
      User updatedUser = identityService.newUser("john");
      updatedUser.setId(invalidId);
      identityService.saveUser(updatedUser);

      fail("Invalid user id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "User", invalidId), ex.getMessage());
    }
  }

  @Test
  public void testInvalidGroupId() {
    String invalidId = "john's group";
    Group group = identityService.newGroup(invalidId);
    try {
      identityService.saveGroup(group);
      fail("Invalid group id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Group", invalidId), ex.getMessage());
    }
  }

  @Test
  public void testInvalidGroupIdOnSave() {
    String invalidId = "john's group";
    try {
      Group updatedGroup = identityService.newGroup("group");
      updatedGroup.setId(invalidId);
      identityService.saveGroup(updatedGroup);

      fail("Invalid group id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Group", invalidId), ex.getMessage());
    }
  }

  @Test
  public void testCamundaAdminId() {
    String camundaAdminID = "camunda-admin";
    try {
      identityService.newUser(camundaAdminID);
      identityService.newGroup(camundaAdminID);
      identityService.newTenant(camundaAdminID);
    } catch (ProcessEngineException ex) {
      fail(camundaAdminID + " should be a valid id.");
    }
  }

  @Test
  public void testCustomResourceWhitelist() {
    processEngine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/api/identity/custom.whitelist.camunda.cfg.xml")
      .buildProcessEngine();

    IdentityService identityService = processEngine.getIdentityService();

    String invalidUserId = "johnDoe";
    String invalidGroupId = "johnsGroup";
    String invalidTenantId = "johnsTenant";

    User user = identityService.newUser(invalidUserId);

    try {
      identityService.saveUser(user);
      fail("Invalid user id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "User", invalidUserId), ex.getMessage());
    }

    Group johnsGroup = identityService.newGroup("johnsGroup");

    try {
      identityService.saveGroup(johnsGroup);
      fail("Invalid group id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Group", invalidGroupId), ex.getMessage());
    }

    Tenant tenant = identityService.newTenant(invalidTenantId);
    try {
      identityService.saveTenant(tenant);
      fail("Invalid tenant id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Tenant", invalidTenantId), ex.getMessage());
    }
  }

  @Test
  public void testSeparateResourceWhitelistPatterns() {
    processEngine = ProcessEngineConfiguration
      .createProcessEngineConfigurationFromResource("org/camunda/bpm/engine/test/api/identity/custom.resource.whitelist.camunda.cfg.xml")
      .buildProcessEngine();

    IdentityService identityService = processEngine.getIdentityService();

    String invalidUserId = "12345";
    String invalidGroupId = "johnsGroup";
    String invalidTenantId = "!@##$%";

    User user = identityService.newUser(invalidUserId);

    // pattern: [a-zA-Z]+
    try {
      identityService.saveUser(user);
      fail("Invalid user id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "User", invalidUserId), ex.getMessage());
    }

    Group group = identityService.newGroup(invalidGroupId);

    // pattern: \d+
    try {
      identityService.saveGroup(group);
      fail("Invalid group id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Group", invalidGroupId), ex.getMessage());
    }

    Tenant tenant = identityService.newTenant(invalidTenantId);
    // new general pattern (used for tenant whitelisting): [a-zA-Z0-9]+
    try {
      identityService.saveTenant(tenant);
      fail("Invalid tenant id exception expected!");
    } catch (ProcessEngineException ex) {
      assertEquals(String.format(INVALID_ID_MESSAGE, "Tenant", invalidTenantId), ex.getMessage());
    }
  }

  @Test
  public void shouldCreateUserWithEmptyUserId() {
    User user = identityService.newUser("");
    assertThat(user).isNotNull();
  }

  @Test
  public void shouldNotIncludePlaintextPasswordInUserToString() {
    // given
    User user = identityService.newUser("id");

    String password = "this is a password";
    user.setPassword(password);

    // when
    String toString = user.toString();

    // then
    assertThat(toString).doesNotContain(password);
  }

  @Test
  public void shouldNotIncludeHashedPasswordAndSaltInUserToString() {
    // given
    User user = identityService.newUser("id");

    String password = "this is a password";
    user.setPassword(password);

    identityService.saveUser(user);

    UserEntity userEntity = (UserEntity) user;
    String salt = userEntity.getSalt();
    String hashedPassword = userEntity.getPassword();

    // when
    String toString = user.toString();

    // then
    assertThat(toString).doesNotContain(salt);
    assertThat(toString).doesNotContain(hashedPassword);

  }

  private Object createStringSet(String... strings) {
    Set<String> stringSet = new HashSet<String>();
    for (String string : strings) {
      stringSet.add(string);
    }
    return stringSet;
  }

  protected Set<String> getGroupIds(List<Group> groups) {
    Set<String> groupIds = new HashSet<String>();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    return groupIds;
  }

  protected Set<String> getUserIds(List<User> users) {
    Set<String> userIds = new HashSet<String>();
    for (User user : users) {
      userIds.add(user.getId());
    }
    return userIds;
  }

}
