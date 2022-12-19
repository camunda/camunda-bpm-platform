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
package org.camunda.bpm.identity.impl.ldap;

import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.camunda.bpm.engine.impl.context.Context.getCommandContext;
import static org.camunda.bpm.engine.impl.context.Context.getProcessEngineConfiguration;
import static org.camunda.bpm.identity.impl.ldap.LdapConfiguration.DB_QUERY_WILDCARD;
import static org.camunda.bpm.identity.impl.ldap.LdapConfiguration.LDAP_QUERY_WILDCARD;

import java.io.IOException;
import java.io.StringWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;


import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.UserQueryProperty;
import org.camunda.bpm.engine.impl.GroupQueryProperty;
import org.camunda.bpm.engine.impl.identity.IdentityProviderException;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.identity.impl.ldap.util.LdapPluginLogger;

/**
 * <p>LDAP {@link ReadOnlyIdentityProvider}.</p>
 *
 * @author Daniel Meyer
 */
public class LdapIdentityProviderSession implements ReadOnlyIdentityProvider {

  protected LdapConfiguration ldapConfiguration;
  // one object of this class is created per thread. One initialContext is created per thread.
  protected LdapContext initialContext;

  public LdapIdentityProviderSession(LdapConfiguration ldapConfiguration) {
    this.ldapConfiguration = ldapConfiguration;
  }

  // Session Lifecycle //////////////////////////////////

  public void flush() {
    // nothing to do
  }

  public void close() {
    closeLdapCtx(initialContext);
  }

  protected void closeLdapCtx(LdapContext context) {
    if (context != null) {
      try {
        context.close();
      } catch (Exception e) {
        // ignore
        LdapPluginLogger.INSTANCE.exceptionWhenClosingLdapCOntext(e);
      }
    }
  }

  protected InitialLdapContext openContext(String userDn, String password) {
    Hashtable<String, String> env = new Hashtable<>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfiguration.getInitialContextFactory());
    env.put(Context.SECURITY_AUTHENTICATION, ldapConfiguration.getSecurityAuthentication());
    env.put(Context.PROVIDER_URL, ldapConfiguration.getServerUrl());
    env.put(Context.SECURITY_PRINCIPAL, userDn);
    env.put(Context.SECURITY_CREDENTIALS, password);

    // for anonymous login
    if (ldapConfiguration.isAllowAnonymousLogin() && password.isEmpty()) {
      env.put(Context.SECURITY_AUTHENTICATION, "none");
    }

    if (ldapConfiguration.isUseSsl()) {
      env.put(Context.SECURITY_PROTOCOL, "ssl");
    }

    // add additional properties
    Map<String, String> contextProperties = ldapConfiguration.getContextProperties();
    if (contextProperties != null) {
      env.putAll(contextProperties);
    }

    try {
      return new InitialLdapContext(env, null);

    } catch (AuthenticationException e) {
      throw new LdapAuthenticationException("Could not authenticate with LDAP server", e);

    } catch (NamingException e) {
      throw new IdentityProviderException("Could not connect to LDAP server", e);

    }
  }

  protected void ensureContextInitialized() {
    if (initialContext == null) {
      initialContext = openContext(ldapConfiguration.getManagerDn(), ldapConfiguration.getManagerPassword());
    }
  }

  // Users /////////////////////////////////////////////////

  public User findUserById(String userId) {
    return createUserQuery(getCommandContext())
            .userId(userId)
            .singleResult();
  }

  public UserQuery createUserQuery() {
    return new LdapUserQueryImpl(getProcessEngineConfiguration().getCommandExecutorTxRequired(), ldapConfiguration);
  }

  public UserQueryImpl createUserQuery(CommandContext commandContext) {
    return new LdapUserQueryImpl(ldapConfiguration);
  }

  @Override
  public NativeUserQuery createNativeUserQuery() {
    throw new BadUserRequestException("Native user queries are not supported for LDAP identity service provider.");
  }

  public long findUserCountByQueryCriteria(LdapUserQueryImpl query) {
    ensureContextInitialized();
    return findUserByQueryCriteria(query).size();
  }

  public List<User> findUserByQueryCriteria(LdapUserQueryImpl query) {
    ensureContextInitialized();

    // convert DB wildcards to LDAP wildcards if necessary
    if (query.getEmailLike() != null) {
      query.userEmailLike(query.getEmailLike().replaceAll(DB_QUERY_WILDCARD, LDAP_QUERY_WILDCARD));
    }
    if (query.getFirstNameLike() != null) {
      query.userFirstNameLike(query.getFirstNameLike().replaceAll(DB_QUERY_WILDCARD, LDAP_QUERY_WILDCARD));
    }
    if (query.getLastNameLike() != null) {
      query.userLastNameLike(query.getLastNameLike().replaceAll(DB_QUERY_WILDCARD, LDAP_QUERY_WILDCARD));
    }

    if (query.getGroupId() != null) {
      // if restriction on groupId is provided, we need to search in group tree first, look for the group and then further restrict on the members
      return findUsersByGroupId(query);
    } else {
      String userBaseDn = composeDn(ldapConfiguration.getUserSearchBase(), ldapConfiguration.getBaseDn());
      return findUsersWithoutGroupId(query, userBaseDn, false);
    }
  }

  protected List<User> findUsersByGroupId(LdapUserQueryImpl query) {
    StringBuilder resultLogger = new StringBuilder();
    if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
      resultLogger.append("findUsersByGroupId: from ");
      resultLogger.append(query.getFirstResult());
    }

    String baseDn = getDnForGroup(query.getGroupId());

    // compose group search filter
    String groupSearchFilter = "(& " + ldapConfiguration.getGroupSearchFilter() + ")";

    NamingEnumeration<SearchResult> enumeration = null;
    try {
      initializeControls(query, resultLogger);

      List<String> groupMemberList = new ArrayList<>();
      int resultCount = 0;
      int pageNumber = 0;

      do {
        enumeration = initialContext.search(baseDn, groupSearchFilter, ldapConfiguration.getSearchControls());
        pageNumber++;
        if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
          resultLogger.append(", (page:");
          resultLogger.append(pageNumber);
          resultLogger.append(")");
        }

        // first find group
        while (enumeration.hasMoreElements()) {
          SearchResult result = enumeration.nextElement();
          Attribute memberAttribute = result.getAttributes().get(ldapConfiguration.getGroupMemberAttribute());
          if (null != memberAttribute) {
            NamingEnumeration<?> allMembers = memberAttribute.getAll();

            // iterate group members
            while (allMembers.hasMoreElements()) {
              if (resultCount >= query.getFirstResult()) {
                groupMemberList.add((String) allMembers.nextElement());
              }
              resultCount++;
            }
          }
        }
      } while (isNextPageDetected(resultLogger) && groupMemberList.size() < query.getMaxResults());

      List<User> userList = new ArrayList<>();
      String userBaseDn = composeDn(ldapConfiguration.getUserSearchBase(), ldapConfiguration.getBaseDn());
      int memberCount = 0;
      for (String memberId : groupMemberList) {
        if (userList.size() < query.getMaxResults() && memberCount >= query.getFirstResult()) {
          if (ldapConfiguration.isUsePosixGroups()) {
            query.userId(memberId);
          }
          List<User> users = ldapConfiguration.isUsePosixGroups() ?
                  findUsersWithoutGroupId(query, userBaseDn, true) :
                  findUsersWithoutGroupId(query, memberId, true);
          if (!users.isEmpty()) {
            userList.add(users.get(0));
          }
        }
        memberCount++;
      }
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("; Result size()=");
        resultLogger.append(userList.size());
        resultLogger.append(" FirstResult=");
        resultLogger.append(userList.isEmpty() ? "--" : userList.get(0).getFirstName() + "]");
      }
      return userList;

    } catch (NamingException e) {
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("; Exception ");
        resultLogger.append(e);
      }
      throw new IdentityProviderException("Could not query for users " + resultLogger, e);
    } finally {
      try {
        if (enumeration != null) {
          enumeration.close();
        }
      } catch (Exception e) {
        // ignore silently
      }
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("]");
        LdapPluginLogger.INSTANCE.userQueryResult(resultLogger.toString());
      }

    }
  }

  public List<User> findUsersWithoutGroupId(LdapUserQueryImpl query, String userBaseDn, boolean ignorePagination) {
    StringBuilder resultLogger = new StringBuilder();
    if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
      resultLogger.append("findUsersWithoutGroupId: from ");
      resultLogger.append(query.getFirstResult());
    }

    NamingEnumeration<SearchResult> enumeration = null;
    try {
      initializeControls(query, resultLogger);

      List<User> userList = new ArrayList<>();
      int resultCount = 0;
      int pageNumber = 0;

      do {
        String filter = getUserSearchFilter(query);
        if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
          resultLogger.append(" search userBaseDn[");
          resultLogger.append(userBaseDn);
          resultLogger.append("] filter[");
          resultLogger.append(filter);
          resultLogger.append("];");
        }
        enumeration = initialContext.search(userBaseDn, filter, ldapConfiguration.getSearchControls());

        pageNumber++;
        // perform client-side paging
        if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
          resultLogger.append(" (page:");
          resultLogger.append(pageNumber);
          resultLogger.append(") ");
        }

        while (enumeration.hasMoreElements()
                && (userList.size() < query.getMaxResults() || ignorePagination)) {
          SearchResult result = enumeration.nextElement();

          UserEntity user = transformUser(result);

          String userId = user.getId();

          if (userId == null) {
            LdapPluginLogger.INSTANCE.invalidLdapUserReturned(user, result);
          } else {
            if (isAuthenticatedUser(user) || isAuthorized(READ, USER, userId)) {

              if (resultCount >= query.getFirstResult() || ignorePagination) {
                if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
                  resultLogger.append("id=");
                  resultLogger.append(user.getId());
                  resultLogger.append(", firstName=");
                  resultLogger.append(user.getFirstName());
                  resultLogger.append(", lastName=");
                  resultLogger.append(user.getLastName());

                  resultLogger.append(" based on ");
                  resultLogger.append(result);
                  resultLogger.append(", ");
                }
                userList.add(user);
              }
              resultCount++;
            }
          }
        }
      } while (isNextPageDetected(resultLogger) && userList.size() < query.getMaxResults());

      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append(";Result size()=");
        resultLogger.append(userList.size());
        resultLogger.append(" First[");
        resultLogger.append(userList.isEmpty() ? "--" : userList.get(0).getFirstName() + "]");
      }
      return userList;

    } catch (NamingException e) {
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append(";Exception: ");
        resultLogger.append(e);
      }
      throw new IdentityProviderException("Could not query for users " + resultLogger, e);
    } finally {
      try {
        if (enumeration != null) {
          enumeration.close();
        }
      } catch (Exception e) {
        // ignore silently
      }

      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("]");
        LdapPluginLogger.INSTANCE.userQueryResult(resultLogger.toString());
      }

    }
  }

  public boolean checkPassword(String userId, String password) {

    // prevent a null password
    if (password == null) {
      return false;
    }

    // engine can't work without users
    if (userId == null || userId.isEmpty()) {
      return false;
    }

    /*
     * We only allow login with no password if anonymous login is set.
     * RFC allows such a behavior but discourages the usage so we provide it for
     * user which have an ldap with anonymous login.
     */
    if (!ldapConfiguration.isAllowAnonymousLogin() && password.equals("")) {
      return false;
    }

    // first search for user using manager DN
    LdapUserEntity user = (LdapUserEntity) findUserById(userId);
    close();

    if (user == null) {
      return false;
    } else {

      LdapContext context = null;

      try {
        // bind authenticate for user + supplied password
        context = openContext(user.getDn(), password);
        return true;

      } catch (LdapAuthenticationException e) {
        return false;

      } finally {
        closeLdapCtx(context);

      }

    }

  }

  protected String getUserSearchFilter(LdapUserQueryImpl query) {

    StringWriter search = new StringWriter();
    search.write("(&");

    // restrict to users
    search.write(ldapConfiguration.getUserSearchFilter());

    // add additional filters from query
    if (query.getId() != null) {
      addFilter(ldapConfiguration.getUserIdAttribute(), escapeLDAPSearchFilter(query.getId()), search);
    }
    if (query.getIds() != null && query.getIds().length > 0) {
      // wrap ids in OR statement
      search.write("(|");
      for (String userId : query.getIds()) {
        addFilter(ldapConfiguration.getUserIdAttribute(), escapeLDAPSearchFilter(userId), search);
      }
      search.write(")");
    }
    if (query.getEmail() != null) {
      addFilter(ldapConfiguration.getUserEmailAttribute(), query.getEmail(), search);
    }
    if (query.getEmailLike() != null) {
      addFilter(ldapConfiguration.getUserEmailAttribute(), query.getEmailLike(), search);
    }
    if (query.getFirstName() != null) {
      addFilter(ldapConfiguration.getUserFirstnameAttribute(), query.getFirstName(), search);
    }
    if (query.getFirstNameLike() != null) {
      addFilter(ldapConfiguration.getUserFirstnameAttribute(), query.getFirstNameLike(), search);
    }
    if (query.getLastName() != null) {
      addFilter(ldapConfiguration.getUserLastnameAttribute(), query.getLastName(), search);
    }
    if (query.getLastNameLike() != null) {
      addFilter(ldapConfiguration.getUserLastnameAttribute(), query.getLastNameLike(), search);
    }

    search.write(")");

    return search.toString();
  }

  // Groups ///////////////////////////////////////////////

  public Group findGroupById(String groupId) {
    return createGroupQuery(getCommandContext())
            .groupId(groupId)
            .singleResult();
  }

  public GroupQuery createGroupQuery() {
    return new LdapGroupQuery(getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  public GroupQuery createGroupQuery(CommandContext commandContext) {
    return new LdapGroupQuery();
  }

  public long findGroupCountByQueryCriteria(LdapGroupQuery ldapGroupQuery) {
    ensureContextInitialized();
    return findGroupByQueryCriteria(ldapGroupQuery).size();
  }

  public List<Group> findGroupByQueryCriteria(LdapGroupQuery query) {

    // convert DB wildcards to LDAP wildcards if necessary
    if (query.getNameLike() != null) {
      query.groupNameLike(query.getNameLike().replaceAll(DB_QUERY_WILDCARD, LDAP_QUERY_WILDCARD));
    }

    StringBuilder resultLogger = new StringBuilder();
    if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
      resultLogger.append("findGroupByQueryCriteria: from ");
      resultLogger.append(query.getFirstResult());
    }
    ensureContextInitialized();

    String groupBaseDn = composeDn(ldapConfiguration.getGroupSearchBase(), ldapConfiguration.getBaseDn());

    NamingEnumeration<SearchResult> enumeration = null;

    try {
      initializeControls(query, resultLogger);
      String filter = getGroupSearchFilter(query);
      // perform client-side paging
      List<Group> groupList = new ArrayList<>();
      int resultCount = 0;
      int pageNumber = 0;

      do {
        enumeration = initialContext.search(groupBaseDn, filter, ldapConfiguration.getSearchControls());
        pageNumber++;

        if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
          resultLogger.append("; (page:");
          resultLogger.append(pageNumber);
          resultLogger.append(") [");
        }

        while (enumeration.hasMoreElements() && groupList.size() < query.getMaxResults()) {
          SearchResult result = enumeration.nextElement();

          GroupEntity group = transformGroup(result);

          String groupId = group.getId();

          if (groupId == null) {
            LdapPluginLogger.INSTANCE.invalidLdapGroupReturned(group, result);
          } else {
            if (isAuthorized(READ, GROUP, groupId)) {

              if (resultCount >= query.getFirstResult()) {
                if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
                  resultLogger.append(group);
                  resultLogger.append(" based on ");
                  resultLogger.append(result);
                  resultLogger.append(", ");
                }
                groupList.add(group);
              }
              resultCount++;
            }
          }
        }
      } while (isNextPageDetected(resultLogger) && groupList.size() < query.getMaxResults());

      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("; Result size()=");
        resultLogger.append(groupList.size());
        resultLogger.append(" FirstResult=");
        resultLogger.append(groupList.isEmpty() ? "--" : groupList.get(0).getName() + "]");
      }
      return groupList;

    } catch (NamingException e) {
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("; Exception ");
        resultLogger.append(e);
      }
      throw new IdentityProviderException("Could not query for users " + resultLogger, e);

    } finally {
      try {
        if (enumeration != null) {
          enumeration.close();
        }
      } catch (Exception e) {
        // ignore silently
      }
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("]");
        LdapPluginLogger.INSTANCE.groupQueryResult(resultLogger.toString());
      }

    }
  }

  protected String getGroupSearchFilter(LdapGroupQuery query) {

    StringWriter search = new StringWriter();
    search.write("(&");

    // restrict to groups
    search.write(ldapConfiguration.getGroupSearchFilter());

    // add additional filters from query
    if (query.getId() != null) {
      addFilter(ldapConfiguration.getGroupIdAttribute(), query.getId(), search);
    }
    if (query.getIds() != null && query.getIds().length > 0) {
      search.write("(|");
      for (String id : query.getIds()) {
        addFilter(ldapConfiguration.getGroupIdAttribute(), id, search);
      }
      search.write(")");
    }
    if (query.getName() != null) {
      addFilter(ldapConfiguration.getGroupNameAttribute(), query.getName(), search);
    }
    if (query.getNameLike() != null) {
      addFilter(ldapConfiguration.getGroupNameAttribute(), query.getNameLike(), search);
    }
    if (query.getUserId() != null) {
      String userDn = null;
      if (ldapConfiguration.isUsePosixGroups()) {
        userDn = query.getUserId();
      } else {
        userDn = getDnForUser(query.getUserId());
      }
      addFilter(ldapConfiguration.getGroupMemberAttribute(), escapeLDAPSearchFilter(userDn), search);
    }
    search.write(")");

    return search.toString();
  }

  // Utils ////////////////////////////////////////////

  protected String getDnForUser(String userId) {
    LdapUserEntity user = (LdapUserEntity) createUserQuery(getCommandContext())
            .userId(userId)
            .singleResult();
    if (user == null) {
      return "";
    } else {
      return user.getDn();
    }
  }

  protected String getDnForGroup(String groupId) {
    LdapGroupEntity group = (LdapGroupEntity) createGroupQuery(getCommandContext())
            .groupId(groupId)
            .singleResult();
    if (group == null) {
      return "";
    } else {
      return group.getDn();
    }
  }

  protected String getStringAttributeValue(String attrName, Attributes attributes) throws NamingException {
    Attribute attribute = attributes.get(attrName);
    if (attribute != null) {
      return (String) attribute.get();
    } else {
      return null;
    }
  }

  protected void addFilter(String attributeName, String attributeValue, StringWriter writer) {
    writer.write("(");
    writer.write(attributeName);
    writer.write("=");
    writer.write(attributeValue);
    writer.write(")");
  }

  protected LdapUserEntity transformUser(SearchResult result) throws NamingException {
    final Attributes attributes = result.getAttributes();
    LdapUserEntity user = new LdapUserEntity();
    user.setDn(result.getNameInNamespace());
    user.setId(getStringAttributeValue(ldapConfiguration.getUserIdAttribute(), attributes));
    user.setFirstName(getStringAttributeValue(ldapConfiguration.getUserFirstnameAttribute(), attributes));
    user.setLastName(getStringAttributeValue(ldapConfiguration.getUserLastnameAttribute(), attributes));
    user.setEmail(getStringAttributeValue(ldapConfiguration.getUserEmailAttribute(), attributes));
    return user;
  }

  protected GroupEntity transformGroup(SearchResult result) throws NamingException {
    final Attributes attributes = result.getAttributes();
    LdapGroupEntity group = new LdapGroupEntity();
    group.setDn(result.getNameInNamespace());
    group.setId(getStringAttributeValue(ldapConfiguration.getGroupIdAttribute(), attributes));
    group.setName(getStringAttributeValue(ldapConfiguration.getGroupNameAttribute(), attributes));
    group.setType(getStringAttributeValue(ldapConfiguration.getGroupTypeAttribute(), attributes));
    return group;
  }

  /**
   * Return the list of Controls requested in the query. Query may be run on USERS or on GROUP
   *
   * @param query query asks, contains the order by requested
   * @return list of control to send to LDAP
   */
  protected List<Control> getSortingControls(AbstractQuery<?, ?> query, StringBuilder resultLogger) {

    try {
      List<Control> controls = new ArrayList<>();

      List<QueryOrderingProperty> orderBy = query.getOrderingProperties();
      if (orderBy != null) {
        for (QueryOrderingProperty orderingProperty : orderBy) {
          String propertyName = orderingProperty.getQueryProperty().getName();
          if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append(", OrderBy[");
            resultLogger.append(propertyName);
            resultLogger.append("-");
            resultLogger.append(orderingProperty.getDirection() == null ? "no_direction(desc)" : orderingProperty.getDirection().getName());
            resultLogger.append("]");
          }
          SortKey sortKey = null;
          if (query instanceof LdapUserQueryImpl) {
            if (UserQueryProperty.USER_ID.getName().equals(propertyName)) {
              sortKey = new SortKey(ldapConfiguration.getUserIdAttribute(), Direction.ASCENDING.equals(orderingProperty.getDirection()),
                  null);

            } else if (UserQueryProperty.EMAIL.getName().equals(propertyName)) {
              sortKey = new SortKey(ldapConfiguration.getUserEmailAttribute(), Direction.ASCENDING.equals(orderingProperty.getDirection()),
                  null);

            } else if (UserQueryProperty.FIRST_NAME.getName().equals(propertyName)) {
              sortKey = new SortKey(ldapConfiguration.getUserFirstnameAttribute(), Direction.ASCENDING.equals(orderingProperty.getDirection()),
                  null);

            } else if (UserQueryProperty.LAST_NAME.getName().equals(propertyName)) {
              sortKey = new SortKey(ldapConfiguration.getUserLastnameAttribute(), Direction.ASCENDING.equals(orderingProperty.getDirection()),
                  null);
            }
          } else if (query instanceof LdapGroupQuery) {
              if (GroupQueryProperty.GROUP_ID.getName().equals(propertyName)) {
                sortKey = new SortKey(ldapConfiguration.getGroupIdAttribute(),
                        Direction.ASCENDING.equals(orderingProperty.getDirection()),
                        null);
              } else if (GroupQueryProperty.NAME.getName().equals(propertyName)) {
                sortKey = new SortKey(ldapConfiguration.getGroupNameAttribute(),
                        Direction.ASCENDING.equals(orderingProperty.getDirection()),
                        null);
              }
              // not possible to order by Type: LDAP may not support the type
          }

          if (sortKey != null) {
            controls.add(new SortControl(new SortKey[] { sortKey }, Control.CRITICAL));
          }
        }
      }

      return controls;

    } catch (IOException e) {
      throw new IdentityProviderException("Exception while setting paging settings", e);
    }
  }

  protected String composeDn(String... parts) {
    StringWriter resultDn = new StringWriter();
    for (String s : parts) {
      String part = s;
      if (part == null || part.length() == 0) {
        continue;
      }
      if (part.endsWith(",")) {
        part = part.substring(part.length() - 2, part.length() - 1);
      }
      if (part.startsWith(",")) {
        part = part.substring(1);
      }
      String currentDn = resultDn.toString();
      if (!currentDn.endsWith(",") && currentDn.length() > 0) {
        resultDn.write(",");
      }
      resultDn.write(part);
    }
    return resultDn.toString();
  }

  /**
   * @return true if the passed-in user is currently authenticated
   */
  protected boolean isAuthenticatedUser(UserEntity user) {
    if (user.getId() == null) {
      return false;
    }
    return user.getId().equalsIgnoreCase(getCommandContext().getAuthenticatedUserId());
  }

  protected boolean isAuthorized(Permission permission, Resource resource, String resourceId) {
    return !ldapConfiguration.isAuthorizationCheckEnabled() || getCommandContext()
            .getAuthorizationManager()
            .isAuthorized(permission, resource, resourceId);
  }

  // Based on https://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java
  protected final String escapeLDAPSearchFilter(String filter) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < filter.length(); i++) {
      char curChar = filter.charAt(i);
      switch (curChar) {
        case '\\':
          sb.append("\\5c");
          break;
        case '*':
          sb.append("\\2a");
          break;
        case '(':
          sb.append("\\28");
          break;
        case ')':
          sb.append("\\29");
          break;
        case '\u0000':
          sb.append("\\00");
          break;
        default:
          sb.append(curChar);
      }
    }
    return sb.toString();
  }

  /**
   * Initializes paged results and sort controls. Might not be supported by all LDAP implementations.
   */
  protected void initializeControls(AbstractQuery<?, ?> query, StringBuilder resultLogger) throws NamingException {
    if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
      resultLogger.append(query.getFirstResult());

      resultLogger.append(" ");
      resultLogger.append(query);
      resultLogger.append(" ");


      resultLogger.append(ldapConfiguration.isSortControlSupported() ? " -sort-" : "-nosort-");

      if (!isPaginationSupported()) {
        resultLogger.append(" -noPagination");

      } else {
        resultLogger.append(" -pagination(");
        resultLogger.append(ldapConfiguration.getPageSize());
        resultLogger.append(")");
      }
    }

    List<Control> listControls = new ArrayList<>();
    if (ldapConfiguration.isSortControlSupported()) {
      listControls.addAll(getSortingControls(query, resultLogger));
    }

    try {
      if (isPaginationSupported()) {
        listControls.add(new PagedResultsControl(getPageSize(), Control.NONCRITICAL));
      }
      if (!listControls.isEmpty()) {
        initialContext.setRequestControls(listControls.toArray(new Control[0]));
      }
    } catch (NamingException | IOException e) {
      // page control is not supported by this LDAP
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("Unsupported page Control;");
      }
      // set supported list controls
      if (!listControls.isEmpty()) {
        try {
          initialContext.setRequestControls(listControls.toArray(new Control[0]));
        } catch (NamingException ne) {
          // this exception is not related to the page control, so throw it
          if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("Unsupported Control;");
          }
          throw ne;
        }
      }
    }
  }

  /**
   * Check in the context if we reach the last page on the query
   *
   * @param resultLogger Logger to send information
   * @return new page detected
   */
  protected boolean isNextPageDetected(StringBuilder resultLogger) {
    // if the pagination is not activated, there isn't a next page.
    if (!isPaginationSupported()) {
      return false;
    }

    try {
      Control[] controls = initialContext.getResponseControls();
      if (controls == null) {
        if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
          resultLogger.append("No-controls-from-the-server");
        }
        return false;
      }

      List<Control> newControlList = new ArrayList<>();
      boolean newPageDetected = false;
      for (Control control : controls) {
        if (control instanceof PagedResultsResponseControl) {
          PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
          byte[] cookie = prrc.getCookie();

          if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
            resultLogger.append("; End-of-page? ");
            resultLogger.append((cookie == null ? "No-more-page" : "Next-page-detected"));
          }
          // Re-activate paged results
          try {
            newControlList.add(new PagedResultsControl(getPageSize(), cookie, Control.CRITICAL));
          } catch (IOException e) {
            if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
              resultLogger.append("Error-when-set-again-the-new-cookie ");
              resultLogger.append(e);
            }
            // stop to loop
            return false;
          }
          newPageDetected = cookie != null;
        }
      }
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("; SetAgain controlList.size()=");
        resultLogger.append(newControlList.size());
      }
      if (!newControlList.isEmpty()) {
        // This is more an UPDATE than a SET. All previous control (sorting) does not need to
        // be set again, the current context keeps them, and the next page is correctly ordered for example.
        // In the current context, just the PageResultControl must be re-set (even this a xxxResultxxx)
        // All another result (SortResponseControl for example) must not be set again, nor the SortControl.
        initialContext.setRequestControls(newControlList.toArray(new Control[0]));
      }
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("; Done. NewPageDetected=");
        resultLogger.append(newPageDetected);
      }

      return newPageDetected;
    } catch (NamingException ne) {
      if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
        resultLogger.append("Could not manage ResponseControl; ");
        resultLogger.append(ne);
      }
      return false;
    }
  }

  protected boolean isPaginationSupported() {
    return getPageSize() != null;
  }

  /**
   * Return the pageSize. Returns null if pagination is disabled.
   *
   * @return the pageSize
   */
  protected Integer getPageSize() {
    return ldapConfiguration.getPageSize();
  }

  @Override
  public TenantQuery createTenantQuery() {
    return new LdapTenantQuery(getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  @Override
  public TenantQuery createTenantQuery(CommandContext commandContext) {
    return new LdapTenantQuery();
  }

  @Override
  public Tenant findTenantById(String id) {
    // since multi-tenancy is not supported for the LDAP plugin, always return null
    return null;
  }
}
