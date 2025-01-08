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
package org.camunda.bpm.spring.boot.starter.security.oauth2.impl;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.GroupQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.TenantQueryImpl;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.identity.IdentityOperationResult;
import org.camunda.bpm.engine.impl.identity.IdentityProviderException;
import org.camunda.bpm.engine.impl.identity.db.DbIdentityServiceProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OAuth2 identity provider with fallback for {@link DbIdentityServiceProvider}
 * if the Spring security context doesn't contain an authenticated user.
 * <p>
 * Since the fallback {@link DbIdentityServiceProvider} is a writeable provider
 * this class is also writeable but with OAuth2 authentication it works effectively as a read-only provider.
 */
public class OAuth2IdentityProvider extends DbIdentityServiceProvider {

  private static final Logger logger = LoggerFactory.getLogger(OAuth2IdentityProvider.class);

  protected static void unsupportedOperationForOAuth2() {
    throw new IdentityProviderException("This operation is not supported for OAuth2 identity provider.");
  }

  protected static void unsupportedFilterForOAuth2() {
    throw new IdentityProviderException("This filter is not supported for OAuth2 identity provider.");
  }

  /**
   * @param searchLike the like value to search for
   * @param value      the actual user attribute value
   * @return true if either values are {@code null} or if {@code value} contains {@code searchLike} (case-insensitive)
   */
  protected static boolean nullOrContainsIgnoreCase(String searchLike, String value) {
    return searchLike == null || value == null || value.toLowerCase()
        .contains(searchLike.replaceAll("%", "").toLowerCase());
  }

  /**
   * @return true if user is authenticated in Spring security context
   */
  protected boolean springSecurityAuthentication() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean springSecurityAuthenticated = authentication != null && authentication.isAuthenticated();
    logger.debug("Using {}", springSecurityAuthenticated ? "OAuth2IdentityProvider" : "DbIdentityServiceProvider");
    return springSecurityAuthenticated;
  }

  protected static UserEntity transformUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    String userId = authentication.getName();
    UserEntity user = new UserEntity();
    user.setId(userId);
    if (principal instanceof OidcUser) {
      var oidcUser = (OidcUser) principal;
      user.setFirstName(oidcUser.getGivenName());
      user.setLastName(oidcUser.getFamilyName());
      user.setEmail(oidcUser.getEmail());
    }
    return user;
  }

  protected static List<Group> transformGroups() {
    return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().map(a -> {
      var group = new GroupEntity();
      group.setId(a.getAuthority());
      group.setName(a.getAuthority());
      return group;
    }).collect(Collectors.toList());
  }

  public static class OAuth2UserQuery extends UserQueryImpl {
    @Override
    public long executeCount(CommandContext commandContext) {
      return 1;
    }

    @Override
    public List<User> executeList(CommandContext commandContext, Page page) {
      if (this.tenantId != null) {
        unsupportedFilterForOAuth2();
      }

      return Stream.of(transformUser())
          .filter(Objects::nonNull)
          .filter(u -> this.id == null || this.id.equals(u.getId()))
          .filter(u -> this.ids == null || Arrays.stream(this.ids).anyMatch(id -> u.getId().equals(id)))
          .filter(u -> this.firstName == null || this.firstName.equals(u.getFirstName()))
          .filter(u -> nullOrContainsIgnoreCase(this.firstNameLike, u.getFirstName()))
          .filter(u -> this.lastName == null || this.lastName.equals(u.getLastName()))
          .filter(u -> nullOrContainsIgnoreCase(this.lastNameLike, u.getLastName()))
          .filter(u -> this.email == null || this.email.equals(u.getEmail()))
          .filter(u -> nullOrContainsIgnoreCase(this.emailLike, u.getEmail()))
          .filter(u -> this.groupId == null || transformGroups().stream().anyMatch(g -> g.getId().equals(this.groupId)))
          .collect(Collectors.toList());
    }
  }

  @Override
  public UserEntity findUserById(String userId) {
    if (springSecurityAuthentication()) {
      var user = transformUser();
      return user != null && Objects.equals(userId, user.getId()) ? user : null;
    } else {
      return super.findUserById(userId);
    }
  }

  @Override
  public UserQuery createUserQuery() {
    return springSecurityAuthentication() ? new OAuth2UserQuery() : super.createUserQuery();
  }

  @Override
  public UserQueryImpl createUserQuery(CommandContext commandContext) {
    return springSecurityAuthentication() ? new OAuth2UserQuery() : super.createUserQuery(commandContext);
  }

  @Override
  public NativeUserQuery createNativeUserQuery() {
    if (springSecurityAuthentication()) {
      unsupportedFilterForOAuth2();
      return null;
    } else {
      return super.createNativeUserQuery();
    }
  }

  @Override
  public boolean checkPassword(String userId, String password) {
    return !springSecurityAuthentication() && super.checkPassword(userId, password);
  }

  public static class OAuth2GroupQuery extends GroupQueryImpl {

    @Override
    public long executeCount(CommandContext commandContext) {
      return executeList(commandContext, null).size();
    }

    @Override
    public List<Group> executeList(CommandContext commandContext, Page page) {
      if (this.type != null || this.tenantId != null) {
        unsupportedFilterForOAuth2();
      }

      return transformGroups().stream()
          .filter(g -> this.id == null || this.id.equals(g.getId()))
          .filter(g -> this.ids == null || Arrays.stream(this.ids).anyMatch(id -> g.getId().equals(id)))
          .filter(g -> this.name == null || this.name.equals(g.getName()))
          .filter(g -> nullOrContainsIgnoreCase(this.nameLike, g.getName()))
          .filter(g -> {
            var user = transformUser();
            return this.userId == null || user == null || this.userId.equals(user.getId());
          })
          .collect(Collectors.toList());
    }
  }

  @Override
  public GroupEntity findGroupById(String groupId) {
    if (springSecurityAuthentication()) {
      var groups = transformGroups();
      return (GroupEntity) groups.stream().filter(g -> g.getId().equals(groupId)).findFirst().orElse(null);
    } else {
      return super.findGroupById(groupId);
    }
  }

  @Override
  public GroupQuery createGroupQuery() {
    return springSecurityAuthentication() ? new OAuth2GroupQuery() : super.createGroupQuery();
  }

  @Override
  public GroupQuery createGroupQuery(CommandContext commandContext) {
    return springSecurityAuthentication() ? new OAuth2GroupQuery() : super.createGroupQuery(commandContext);
  }

  public static class OAuth2TenantQuery extends TenantQueryImpl {
    @Override
    public long executeCount(CommandContext commandContext) {
      return 0;
    }

    @Override
    public List<Tenant> executeList(CommandContext commandContext, Page page) {
      return Collections.emptyList();
    }
  }

  @Override
  public TenantEntity findTenantById(String tenantId) {
    return springSecurityAuthentication() ? null : super.findTenantById(tenantId);
  }

  @Override
  public TenantQuery createTenantQuery() {
    return springSecurityAuthentication() ? new OAuth2TenantQuery() : super.createTenantQuery();
  }

  @Override
  public TenantQuery createTenantQuery(CommandContext commandContext) {
    return springSecurityAuthentication() ? new OAuth2TenantQuery() : super.createTenantQuery();
  }

  @Override
  public void flush() {
    if (!springSecurityAuthentication()) {
      super.flush();
    } // else nothing to do
  }

  @Override
  public void close() {
    if (!springSecurityAuthentication()) {
      super.flush();
    } // else nothing to do
  }

  // WriteableIdentityProvider methods

  @Override
  public UserEntity createNewUser(String userId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.createNewUser(userId);
  }

  @Override
  public IdentityOperationResult saveUser(User user) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.saveUser(user);
  }

  @Override
  public IdentityOperationResult deleteUser(String userId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.deleteUser(userId);
  }

  @Override
  public IdentityOperationResult unlockUser(String userId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.unlockUser(userId);
  }

  @Override
  public GroupEntity createNewGroup(String groupId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.createNewGroup(groupId);
  }

  @Override
  public IdentityOperationResult saveGroup(Group group) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.saveGroup(group);
  }

  @Override
  public IdentityOperationResult deleteGroup(String groupId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.deleteGroup(groupId);
  }

  @Override
  public Tenant createNewTenant(String tenantId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.createNewTenant(tenantId);
  }

  @Override
  public IdentityOperationResult saveTenant(Tenant tenant) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.saveTenant(tenant);
  }

  @Override
  public IdentityOperationResult deleteTenant(String tenantId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.deleteTenant(tenantId);
  }

  @Override
  public IdentityOperationResult createMembership(String userId, String groupId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.createMembership(userId, groupId);
  }

  @Override
  public IdentityOperationResult deleteMembership(String userId, String groupId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.deleteMembership(userId, groupId);
  }

  @Override
  public IdentityOperationResult createTenantUserMembership(String tenantId, String userId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.createTenantUserMembership(tenantId, userId);
  }

  @Override
  public IdentityOperationResult createTenantGroupMembership(String tenantId, String groupId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.createTenantGroupMembership(tenantId, groupId);
  }

  @Override
  public IdentityOperationResult deleteTenantUserMembership(String tenantId, String userId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.deleteTenantUserMembership(tenantId, userId);
  }

  @Override
  public IdentityOperationResult deleteTenantGroupMembership(String tenantId, String groupId) {
    if (springSecurityAuthentication()) {
      unsupportedOperationForOAuth2();
    }
    return super.deleteTenantGroupMembership(tenantId, groupId);
  }

}