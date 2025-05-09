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

import java.io.StringWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortKey;
import javax.naming.ldap.PagedResultsResponseControl;

import org.camunda.bpm.engine.BadUserRequestException;
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
import org.camunda.bpm.engine.impl.db.DbEntity;
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

  protected LdapClient ldapClient;

  public LdapIdentityProviderSession(LdapConfiguration ldapConfiguration) {
    this.ldapConfiguration = ldapConfiguration;
    this.ldapClient = new LdapClient(ldapConfiguration);
  }

  // Session Lifecycle //////////////////////////////////

  public void flush() {
    // nothing to do
  }

  public void close() {
    ldapClient.closeLdapCtx();
  }

  // Users /////////////////////////////////////////////////

  public User findUserById(String userId) {
    return createUserQuery(getCommandContext()).userId(userId).singleResult();
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
    ldapClient.ensureContextInitialized();
    return findUserByQueryCriteria(query).size();
  }

  public List<User> findUserByQueryCriteria(LdapUserQueryImpl query) {
    ldapClient.ensureContextInitialized();

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

  protected boolean paginationContinues(int currentSize, int maxResults) {
    return nextPageDetected() && currentSize < maxResults;
  }

  protected List<User> findUsersByGroupId(LdapUserQueryImpl query) {
    String baseDn = getDnForGroup(query.getGroupId());

    // compose group search filter
    String groupSearchFilter = "(& " + ldapConfiguration.getGroupSearchFilter() + ")";

    initializeControls(query);

    List<String> groupMembers = new ArrayList<>();
    int resultCount = 0;

    do {
      try (LdapSearchResults searchResults = ldapClient.search(baseDn, groupSearchFilter)) {
        // first find group
        while (searchResults.hasMoreElements()) {
          String groupMemberAttribute = ldapConfiguration.getGroupMemberAttribute();
          NamingEnumeration<String> allGroupMembers = LdapClient.getAllMembers(groupMemberAttribute, searchResults);
          if (allGroupMembers != null) {
            // iterate group members
            while (allGroupMembers.hasMoreElements()) {
              if (resultCount >= query.getFirstResult()) {
                groupMembers.add(allGroupMembers.nextElement());
              }
              resultCount++;
            }
          }
        }
      }
    } while (paginationContinues(groupMembers.size(), query.getMaxResults()));

    List<User> userList = new ArrayList<>();
    String userBaseDn = composeDn(ldapConfiguration.getUserSearchBase(), ldapConfiguration.getBaseDn());
    int memberCount = 0;
    for (String memberId : groupMembers) {
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

    return userList;
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
     * RFC allows such a behavior but discourages the usage, so we provide it for
     * user which have a ldap with anonymous login.
     */
    if (!ldapConfiguration.isAllowAnonymousLogin() && password.isEmpty()) {
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
        context = ldapClient.openContext(user.getDn(), password);
        return true;

      } catch (LdapAuthenticationException e) {
        if(ldapConfiguration.isPasswordCheckCatchAuthenticationException()) {
          return false;
        } else {
          throw e;
        }

      } finally {
        ldapClient.closeLdapCtx(context);

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
  protected boolean isAuthenticatedAndAuthorized(String userId) {
    return isAuthenticatedUser(userId) || isAuthorizedToRead(USER, userId);
  }

  public List<User> findUsersWithoutGroupId(LdapUserQueryImpl query, String userBaseDn, boolean ignorePagination) {
    initializeControls(query);

    return retrieveResults(userBaseDn,
        getUserSearchFilter(query),
        this::transformUser,
        this::isAuthenticatedAndAuthorized,
        query.getMaxResults(),
        query.getFirstResult(),
        ignorePagination);
  }

  // Groups ///////////////////////////////////////////////

  public Group findGroupById(String groupId) {
    return createGroupQuery(getCommandContext()).groupId(groupId).singleResult();
  }

  public GroupQuery createGroupQuery() {
    return new LdapGroupQuery(getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  public GroupQuery createGroupQuery(CommandContext commandContext) {
    return new LdapGroupQuery();
  }

  public long findGroupCountByQueryCriteria(LdapGroupQuery ldapGroupQuery) {
    ldapClient.ensureContextInitialized();
    return findGroupByQueryCriteria(ldapGroupQuery).size();
  }

  protected boolean isAuthorizedToReadGroup(String groupId) {
    return isAuthorizedToRead(GROUP, groupId);
  }

  public List<Group> findGroupByQueryCriteria(LdapGroupQuery query) {

    // convert DB wildcards to LDAP wildcards if necessary
    if (query.getNameLike() != null) {
      query.groupNameLike(query.getNameLike().replaceAll(DB_QUERY_WILDCARD, LDAP_QUERY_WILDCARD));
    }

    ldapClient.ensureContextInitialized();

    String groupBaseDn = composeDn(ldapConfiguration.getGroupSearchBase(), ldapConfiguration.getBaseDn());

    initializeControls(query);

    return retrieveResults(groupBaseDn,
        getGroupSearchFilter(query),
        this::transformGroup,
        this::isAuthorizedToReadGroup,
        query.getMaxResults(),
        query.getFirstResult(),
        false);
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

  @SuppressWarnings("unchecked")
  protected <E extends DbEntity, T> List<T> retrieveResults(String baseDn,
                                                            String filter,
                                                            Function<SearchResult, E> transformEntity,
                                                            Predicate<String> resultCountPredicate,
                                                            int maxResults,
                                                            int firstResult,
                                                            boolean ignorePagination) {
    StringBuilder resultLogger = new StringBuilder();
    if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
      resultLogger.append("LDAP query results: [");
    }

    List<T> entities = new ArrayList<>();

    int resultCount = 0;

    do {
      try (LdapSearchResults searchResults = ldapClient.search(baseDn, filter)) {
        while (searchResults.hasMoreElements() && (entities.size() < maxResults || ignorePagination)) {
          SearchResult result = searchResults.nextElement();

          E entity = transformEntity.apply(result);

          String id = entity.getId();
          if (id == null) {
            LdapPluginLogger.INSTANCE.invalidLdapEntityReturned(entity, result);
          } else {
            if (resultCountPredicate.test(id)) {
              if (resultCount >= firstResult || ignorePagination) {
                if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
                  resultLogger.append(entity);
                  resultLogger.append(" based on ");
                  resultLogger.append(result);
                  resultLogger.append(", ");
                }
                entities.add((T) entity);
              }
              resultCount++;
            }
          }
        }
      }
    } while (paginationContinues(entities.size(), maxResults));

    if (LdapPluginLogger.INSTANCE.isDebugEnabled()) {
      resultLogger.append("]");
      LdapPluginLogger.INSTANCE.queryResult(resultLogger.toString());
    }

    return entities;
  }

  protected String getDnForUser(String userId) {
    LdapUserEntity user = (LdapUserEntity) createUserQuery(getCommandContext()).userId(userId).singleResult();
    if (user == null) {
      return "";
    } else {
      return user.getDn();
    }
  }

  protected String getDnForGroup(String groupId) {
    LdapGroupEntity group = (LdapGroupEntity) createGroupQuery(getCommandContext()).groupId(groupId).singleResult();
    if (group == null) {
      return "";
    } else {
      return group.getDn();
    }
  }

  protected void addFilter(String attributeName, String attributeValue, StringWriter writer) {
    writer.write("(");
    writer.write(attributeName);
    writer.write("=");
    writer.write(attributeValue);
    writer.write(")");
  }

  protected UserEntity transformUser(SearchResult result) {
    final Attributes attributes = result.getAttributes();
    LdapUserEntity user = new LdapUserEntity();
    user.setDn(result.getNameInNamespace());
    user.setId(LdapClient.getValue(ldapConfiguration.getUserIdAttribute(), attributes));
    user.setFirstName(LdapClient.getValue(ldapConfiguration.getUserFirstnameAttribute(), attributes));
    user.setLastName(LdapClient.getValue(ldapConfiguration.getUserLastnameAttribute(), attributes));
    user.setEmail(LdapClient.getValue(ldapConfiguration.getUserEmailAttribute(), attributes));
    return user;
  }

  protected GroupEntity transformGroup(SearchResult result) {
    final Attributes attributes = result.getAttributes();
    LdapGroupEntity group = new LdapGroupEntity();
    group.setDn(result.getNameInNamespace());
    group.setId(LdapClient.getValue(ldapConfiguration.getGroupIdAttribute(), attributes));
    group.setName(LdapClient.getValue(ldapConfiguration.getGroupNameAttribute(), attributes));
    group.setType(LdapClient.getValue(ldapConfiguration.getGroupTypeAttribute(), attributes));
    return group;
  }

  /**
   * Return the list of Controls requested in the query. Query may be run on USERS or on GROUP
   *
   * @param query query asks, contains the order by requested
   * @return list of control to send to LDAP
   */
  protected List<Control> getSortingControls(AbstractQuery<?, ?> query) {
    List<Control> controls = new ArrayList<>();

    List<QueryOrderingProperty> orderBy = query.getOrderingProperties();
    if (orderBy != null) {
      for (QueryOrderingProperty orderingProperty : orderBy) {
        String propertyName = orderingProperty.getQueryProperty().getName();
        SortKey sortKey = getSortKey(query, propertyName, orderingProperty);

        if (sortKey != null) {
          LdapClient.addSortKey(sortKey, controls);
        }
      }
    }

    return controls;
  }

  protected SortKey getSortKey(AbstractQuery<?, ?> query, String propertyName, QueryOrderingProperty orderingProperty) {
    if (query instanceof LdapUserQueryImpl) {
      if (UserQueryProperty.USER_ID.getName().equals(propertyName)) {
        return new SortKey(ldapConfiguration.getUserIdAttribute(),
            Direction.ASCENDING.equals(orderingProperty.getDirection()), null);

      } else if (UserQueryProperty.EMAIL.getName().equals(propertyName)) {
        return new SortKey(ldapConfiguration.getUserEmailAttribute(),
            Direction.ASCENDING.equals(orderingProperty.getDirection()), null);

      } else if (UserQueryProperty.FIRST_NAME.getName().equals(propertyName)) {
        return new SortKey(ldapConfiguration.getUserFirstnameAttribute(),
            Direction.ASCENDING.equals(orderingProperty.getDirection()), null);

      } else if (UserQueryProperty.LAST_NAME.getName().equals(propertyName)) {
        return new SortKey(ldapConfiguration.getUserLastnameAttribute(),
            Direction.ASCENDING.equals(orderingProperty.getDirection()), null);
      }
    } else if (query instanceof LdapGroupQuery) {
      if (GroupQueryProperty.GROUP_ID.getName().equals(propertyName)) {
        return new SortKey(ldapConfiguration.getGroupIdAttribute(),
            Direction.ASCENDING.equals(orderingProperty.getDirection()), null);
      } else if (GroupQueryProperty.NAME.getName().equals(propertyName)) {
        return new SortKey(ldapConfiguration.getGroupNameAttribute(),
            Direction.ASCENDING.equals(orderingProperty.getDirection()), null);
      }
      // not possible to order by Type: LDAP may not support the type
    }

    return null;
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
  protected boolean isAuthenticatedUser(String userid) {
    if (userid == null) {
      return false;
    }
    return userid.equalsIgnoreCase(getCommandContext().getAuthenticatedUserId());
  }

  protected boolean isAuthorizedToRead(Resource resource, String resourceId) {
    return !ldapConfiguration.isAuthorizationCheckEnabled() ||
        getCommandContext().getAuthorizationManager()
            .isAuthorized(READ, resource, resourceId);
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
  protected void initializeControls(AbstractQuery<?, ?> query) {
    List<Control> listControls = new ArrayList<>();
    if (ldapConfiguration.isSortControlSupported()) {
      listControls.addAll(getSortingControls(query));
    }

    try {
      if (isPaginationSupported()) {
        LdapClient.addPaginationControl(listControls, null, getPageSize());
      }
    } catch (IdentityProviderException ignored) {
      // Ignore exception when pagination is not supported
    } finally {
      if (!listControls.isEmpty()) {
        ldapClient.setRequestControls(listControls);
      }
    }
  }

  /**
   * Check in the context if we reach the last page on the query
   *
   * @return new page detected
   */
  protected boolean nextPageDetected() {
    // if the pagination is not activated, there isn't a next page.
    if (!isPaginationSupported()) {
      return false;
    }

    Control[] controls = ldapClient.getResponseControls();
    if (controls == null) {
      return false;
    }

    List<Control> newControlList = new ArrayList<>();
    boolean newPageDetected = false;
    for (Control control : controls) {
      if (control instanceof PagedResultsResponseControl) {
        PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
        byte[] cookie = prrc.getCookie();

        // Re-activate paged results
        try {
          LdapClient.addPaginationControl(newControlList, cookie, getPageSize());
        } catch (IdentityProviderException e) {
          return false;
        }
        newPageDetected = cookie != null;
      }
    }

    if (!newControlList.isEmpty()) {
      // This is more an UPDATE than a SET. All previous control (sorting) does not need to
      // be set again, the current context keeps them, and the next page is correctly ordered for example.
      // In the current context, just the PageResultControl must be re-set (even this a xxxResultxxx)
      // All another result (SortResponseControl for example) must not be set again, nor the SortControl.
      ldapClient.setRequestControls(newControlList);
    }

    return newPageDetected;
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
