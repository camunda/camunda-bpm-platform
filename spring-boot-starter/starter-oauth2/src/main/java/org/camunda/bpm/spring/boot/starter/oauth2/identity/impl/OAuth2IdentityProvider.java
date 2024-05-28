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
package org.camunda.bpm.spring.boot.starter.oauth2.identity.impl;

import org.camunda.bpm.engine.BadUserRequestException;
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
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TenantEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OAuth2IdentityProvider implements ReadOnlyIdentityProvider {

  protected static User transformUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null)
      return null;

    var userId = authentication.getName();
    var oidcUser = (OidcUser) authentication.getPrincipal();
    var user = new UserEntity();
    user.setId(userId);
    user.setFirstName(oidcUser.getGivenName());
    user.setLastName(oidcUser.getFamilyName());
    user.setEmail(oidcUser.getEmail());
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
      var user = transformUser();
      
      if (user == null) return null;
      if (id != null && !id.equals(user.getId())) return null;
      if (ids != null && Arrays.stream(ids).noneMatch(id -> id.equals(user.getId()))) return null;
      // TODO handle other filters
      return List.of(user);
    }
    
  }

  @Override
  public User findUserById(String userId) {
    var user = transformUser();
    return user != null && Objects.equals(userId, user.getId()) ? user : null;
  }

  @Override
  public UserQuery createUserQuery() {
    return new OAuth2UserQuery();
  }

  @Override
  public UserQueryImpl createUserQuery(CommandContext commandContext) {
    return new OAuth2UserQuery();
  }

  @Override
  public NativeUserQuery createNativeUserQuery() {
    throw new BadUserRequestException("Native user queries are not supported for OAuth2 identity service provider.");
  }

  @Override
  public boolean checkPassword(String userId, String password) {
    return false;
  }

  public static class OAuth2GroupQuery extends GroupQueryImpl {

    @Override
    public long executeCount(CommandContext commandContext) {
      return executeList(commandContext, null).size();
    }

    @Override
    public List<Group> executeList(CommandContext commandContext, Page page) {
      return transformGroups().stream()
          .filter(g -> this.id == null || this.id.equals(g.getId()))
          .filter(g -> this.ids == null || Arrays.stream(this.ids).anyMatch(id -> g.getId().equals(id)))
          // TODO handle other filters
          .collect(Collectors.toList());
    }
  }

  @Override
  public GroupEntity findGroupById(String groupId) {
    var groups = transformGroups();
    return (GroupEntity) groups.stream().filter(g -> g.getId().equals(groupId)).findFirst().orElse(null);
  }

  @Override
  public GroupQuery createGroupQuery() {
    return new OAuth2GroupQuery();
  }

  @Override
  public GroupQuery createGroupQuery(CommandContext commandContext) {
    return new OAuth2GroupQuery();
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

  public TenantEntity findTenantById(String tenantId) {
    return null;
  }

  public TenantQuery createTenantQuery() {
    return new OAuth2TenantQuery();
  }

  public TenantQuery createTenantQuery(CommandContext commandContext) {
    return new OAuth2TenantQuery();
  }

  @Override
  public void flush() {
    // nothing to do
  }

  @Override
  public void close() {
    // nothing to do
  }
}
