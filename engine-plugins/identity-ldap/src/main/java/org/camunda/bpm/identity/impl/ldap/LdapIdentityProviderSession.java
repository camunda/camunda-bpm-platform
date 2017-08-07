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

import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.GROUP;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.camunda.bpm.engine.impl.identity.IdentityProviderException;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.GroupEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;

/**
 * <p>LDAP {@link ReadOnlyIdentityProvider}.</p>
 *
 * @author Daniel Meyer
 *
 */
public class LdapIdentityProviderSession implements ReadOnlyIdentityProvider {

  private final static Logger LOG = Logger.getLogger(LdapIdentityProviderSession.class.getName());

  protected LdapConfiguration ldapConfiguration;
  protected LdapContext initialContext;

  public LdapIdentityProviderSession(LdapConfiguration ldapConfiguration) {
    this.ldapConfiguration = ldapConfiguration;
  }

  // Session Lifecycle //////////////////////////////////

  public void flush() {
    // nothing to do
  }

  public void close() {
    if (initialContext != null) {
      try {
        initialContext.close();
      } catch (Exception e) {
        // ignore
        LOG.log(Level.FINE, "exception while closing LDAP DIR CTX", e);
      }
    }
  }

  protected InitialLdapContext openContext(String userDn, String password) {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfiguration.getInitialContextFactory());
    env.put(Context.SECURITY_AUTHENTICATION, ldapConfiguration.getSecurityAuthentication());
    env.put(Context.PROVIDER_URL, ldapConfiguration.getServerUrl());
    env.put(Context.SECURITY_PRINCIPAL, userDn);
    env.put(Context.SECURITY_CREDENTIALS, password);

    // for anonymous login
    if(ldapConfiguration.isAllowAnonymousLogin() && password.isEmpty()) {
      env.put(Context.SECURITY_AUTHENTICATION, "none");
    }

    if(ldapConfiguration.isUseSsl()) {
      env.put(Context.SECURITY_PROTOCOL, "ssl");
    }

    // add additional properties
    Map<String, String> contextProperties = ldapConfiguration.getContextProperties();
    if(contextProperties != null) {
      env.putAll(contextProperties);
    }

    try {
      return new InitialLdapContext(env, null);

    } catch(AuthenticationException e) {
      throw new LdapAuthenticationException("Could not authenticate with LDAP server", e);

    } catch(NamingException e) {
      throw new IdentityProviderException("Could not connect to LDAP server", e);

    }
  }

  protected void ensureContextInitialized() {
    if(initialContext == null) {
      initialContext = openContext(ldapConfiguration.getManagerDn(), ldapConfiguration.getManagerPassword());
    }
  }

  // Users /////////////////////////////////////////////////

  public User findUserById(String userId) {
    return createUserQuery(org.camunda.bpm.engine.impl.context.Context.getCommandContext())
      .userId(userId)
      .singleResult();
  }

  public UserQuery createUserQuery() {
    return new LdapUserQueryImpl(org.camunda.bpm.engine.impl.context.Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  public UserQueryImpl createUserQuery(CommandContext commandContext) {
    return new LdapUserQueryImpl();
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
    if(query.getGroupId() != null) {
      // if restriction on groupId is provided, we need to search in group tree first, look for the group and then further restrict on the members
      return findUsersByGroupId(query);
    } else {
      String userBaseDn = composeDn(ldapConfiguration.getUserSearchBase(), ldapConfiguration.getBaseDn());
      return findUsersWithoutGroupId(query, userBaseDn, false);
    }
  }

  protected List<User> findUsersByGroupId(LdapUserQueryImpl query) {
    String baseDn = getDnForGroup(query.getGroupId());

    // compose group search filter
    String groupSearchFilter = "(& " + ldapConfiguration.getGroupSearchFilter() + ")";

    NamingEnumeration<SearchResult> enumeration = null;
    try {
      enumeration = initialContext.search(baseDn, groupSearchFilter, ldapConfiguration.getSearchControls());

      List<String> groupMemberList = new ArrayList<String>();

      // first find group
      while (enumeration.hasMoreElements()) {
        SearchResult result = enumeration.nextElement();
        Attribute memberAttribute = result.getAttributes().get(ldapConfiguration.getGroupMemberAttribute());
        if (null != memberAttribute) {
          NamingEnumeration<?> allMembers = memberAttribute.getAll();

          // iterate group members
          while (allMembers.hasMoreElements()) {
            groupMemberList.add((String) allMembers.nextElement());
          }
        }
      }

      List<User> userList = new ArrayList<User>();
      String userBaseDn = composeDn(ldapConfiguration.getUserSearchBase(), ldapConfiguration.getBaseDn());
      int memberCount = 0;
      for (String memberId : groupMemberList) {
        if (userList.size() < query.getMaxResults() && memberCount >= query.getFirstResult()) {
          if (ldapConfiguration.isUsePosixGroups()) {
            query.userId(memberId);
          }
          List<User> users = ldapConfiguration.isUsePosixGroups() ? findUsersWithoutGroupId(query, userBaseDn, true) : findUsersWithoutGroupId(query, memberId, true);
          if (users.size() > 0) {
            userList.add(users.get(0));
          }
        }
        memberCount++;
      }

      return userList;

    } catch (NamingException e) {
      throw new IdentityProviderException("Could not query for users", e);

    } finally {
      try {
        if (enumeration != null) {
          enumeration.close();
        }
      } catch (Exception e) {
        // ignore silently
      }
    }
  }

  public List<User> findUsersWithoutGroupId(LdapUserQueryImpl query, String userBaseDn, boolean ignorePagination) {

    if(ldapConfiguration.isSortControlSupported()) {
      applyRequestControls(query);
    }

    NamingEnumeration<SearchResult> enumeration = null;
    try {

      String filter = getUserSearchFilter(query);
      enumeration = initialContext.search(userBaseDn, filter, ldapConfiguration.getSearchControls());

      // perform client-side paging
      int resultCount = 0;
      List<User> userList = new ArrayList<User>();
      while (enumeration.hasMoreElements() && (userList.size() < query.getMaxResults() || ignorePagination)) {
        SearchResult result = enumeration.nextElement();

        UserEntity user = transformUser(result);

        if(isAuthenticatedUser(user) || isAuthorized(READ, USER, user.getId())) {

          if(resultCount >= query.getFirstResult() || ignorePagination) {
            userList.add(user);
          }

          resultCount ++;
        }
      }

      return userList;

    } catch (NamingException e) {
      throw new IdentityProviderException("Could not query for users", e);

    } finally {
      try {
        if (enumeration != null) {
          enumeration.close();
        }
      } catch (Exception e) {
        // ignore silently
      }
    }
  }

  public boolean checkPassword(String userId, String password) {

    // prevent a null password
    if(password == null) {
      return false;
    }

    // engine can't work without users
    if(userId == null || userId.isEmpty()) {
      return false;
    }

    /*
    * We only allow login with no password if anonymous login is set.
    * RFC allows such a behavior but discourages the usage so we provide it for
    * user which have an ldap with anonymous login.
    */
    if(!ldapConfiguration.isAllowAnonymousLogin() && password.equals("")) {
      return false;
    }

    // first search for user using manager DN
    LdapUserEntity user = (LdapUserEntity) findUserById(userId);
    close();

    if(user == null) {
      return false;
    } else {

      try {
        // bind authenticate for user + supplied password
        openContext(user.getDn(), password);
        return true;

      } catch(LdapAuthenticationException e) {
        return false;

      }

    }

  }

  protected String getUserSearchFilter(LdapUserQueryImpl query) {

    StringWriter search = new StringWriter();
    search.write("(&");

    // restrict to users
    search.write(ldapConfiguration.getUserSearchFilter());

    // add additional filters from query
    if(query.getId() != null) {
      addFilter(ldapConfiguration.getUserIdAttribute(), escapeLDAPSearchFilter(query.getId()), search);
    }
    if(query.getIds() != null && query.getIds().length > 0) {
      // wrap ids in OR statement
      search.write("(|");
      for (String userId : query.getIds()) {
        addFilter(ldapConfiguration.getUserIdAttribute(), escapeLDAPSearchFilter(userId), search);
      }
      search.write(")");
    }
    if(query.getEmail() != null) {
      addFilter(ldapConfiguration.getUserEmailAttribute(), query.getEmail(), search);
    }
    if(query.getEmailLike() != null) {
      addFilter(ldapConfiguration.getUserEmailAttribute(), query.getEmailLike(), search);
    }
    if(query.getFirstName() != null) {
      addFilter(ldapConfiguration.getUserFirstnameAttribute(), query.getFirstName(), search);
    }
    if(query.getFirstNameLike() != null) {
      addFilter(ldapConfiguration.getUserFirstnameAttribute(), query.getFirstNameLike(), search);
    }
    if(query.getLastName() != null) {
      addFilter(ldapConfiguration.getUserLastnameAttribute(), query.getLastName(), search);
    }
    if(query.getLastNameLike() != null) {
      addFilter(ldapConfiguration.getUserLastnameAttribute(), query.getLastNameLike(), search);
    }

    search.write(")");

    return search.toString();
  }

  // Groups ///////////////////////////////////////////////

  public Group findGroupById(String groupId) {
    return createGroupQuery(org.camunda.bpm.engine.impl.context.Context.getCommandContext())
      .groupId(groupId)
      .singleResult();
  }

  public GroupQuery createGroupQuery() {
    return new LdapGroupQuery(org.camunda.bpm.engine.impl.context.Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  public GroupQuery createGroupQuery(CommandContext commandContext) {
    return new LdapGroupQuery();
  }

  public long findGroupCountByQueryCriteria(LdapGroupQuery ldapGroupQuery) {
    ensureContextInitialized();
    return findGroupByQueryCriteria(ldapGroupQuery).size();
  }

  public List<Group> findGroupByQueryCriteria(LdapGroupQuery query) {
    ensureContextInitialized();

    String groupBaseDn = composeDn(ldapConfiguration.getGroupSearchBase(), ldapConfiguration.getBaseDn());

    if(ldapConfiguration.isSortControlSupported()) {
      applyRequestControls(query);
    }

    NamingEnumeration<SearchResult> enumeration = null;
    try {

      String filter = getGroupSearchFilter(query);
      enumeration = initialContext.search(groupBaseDn, filter, ldapConfiguration.getSearchControls());

      // perform client-side paging
      int resultCount = 0;
      List<Group> groupList = new ArrayList<Group>();
      while (enumeration.hasMoreElements() && groupList.size() < query.getMaxResults()) {
        SearchResult result = enumeration.nextElement();

        GroupEntity group = transformGroup(result);

        if(isAuthorized(READ, GROUP, group.getId())) {

          if(resultCount >= query.getFirstResult()) {
            groupList.add(group);
          }

          resultCount ++;
        }

      }

      return groupList;

    } catch (NamingException e) {
      throw new IdentityProviderException("Could not query for users", e);

    } finally {
      try {
        if (enumeration != null) {
          enumeration.close();
        }
      } catch (Exception e) {
        // ignore silently
      }
    }
  }

  protected String getGroupSearchFilter(LdapGroupQuery query) {

    StringWriter search = new StringWriter();
    search.write("(&");

    // restrict to groups
    search.write(ldapConfiguration.getGroupSearchFilter());

    // add additional filters from query
    if(query.getId() != null) {
      addFilter(ldapConfiguration.getGroupIdAttribute(), query.getId(), search);
    }
    if(query.getIds() != null && query.getIds().length > 0) {
      search.write("(|");
      for (String id : query.getIds()) {
        addFilter(ldapConfiguration.getGroupIdAttribute(), id, search);
      }
      search.write(")");
    }
    if(query.getName() != null) {
      addFilter(ldapConfiguration.getGroupNameAttribute(), query.getName(), search);
    }
    if(query.getNameLike() != null) {
      addFilter(ldapConfiguration.getGroupNameAttribute(), query.getNameLike(), search);
    }
    if(query.getUserId() != null) {
      String userDn = null;
      if(ldapConfiguration.isUsePosixGroups()) {
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
    LdapUserEntity user = (LdapUserEntity) createUserQuery(org.camunda.bpm.engine.impl.context.Context.getCommandContext())
      .userId(userId)
      .singleResult();
    if(user == null) {
      return "";
    } else {
      return user.getDn();
    }
  }

  protected String getDnForGroup(String groupId) {
    LdapGroupEntity group = (LdapGroupEntity) createGroupQuery(org.camunda.bpm.engine.impl.context.Context.getCommandContext())
      .groupId(groupId)
      .singleResult();
    if(group == null) {
      return "";
    } else {
      return group.getDn();
    }
  }

  protected String getStringAttributeValue(String attrName, Attributes attributes) throws NamingException {
    Attribute attribute = attributes.get(attrName);
    if(attribute != null){
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

  protected void applyRequestControls(AbstractQuery<?, ?> query) {

    try {
      List<Control> controls = new ArrayList<Control>();

      List<QueryOrderingProperty> orderBy = query.getOrderingProperties();
      if(orderBy != null) {
        for (QueryOrderingProperty orderingProperty : orderBy) {
          String propertyName = orderingProperty.getQueryProperty().getName();
          if(UserQueryProperty.USER_ID.getName().equals(propertyName)) {
            controls.add(new SortControl(ldapConfiguration.getUserIdAttribute(), Control.CRITICAL));

          } else if(UserQueryProperty.EMAIL.getName().equals(propertyName)) {
            controls.add(new SortControl(ldapConfiguration.getUserEmailAttribute(), Control.CRITICAL));

          } else if(UserQueryProperty.FIRST_NAME.getName().equals(propertyName)) {
            controls.add(new SortControl(ldapConfiguration.getUserFirstnameAttribute(), Control.CRITICAL));

          } else if(UserQueryProperty.LAST_NAME.getName().equals(propertyName)) {
            controls.add(new SortControl(ldapConfiguration.getUserLastnameAttribute(), Control.CRITICAL));
          }
        }
      }

      initialContext.setRequestControls(controls.toArray(new Control[0]));

    } catch (Exception e) {
      throw new IdentityProviderException("Exception while setting paging settings", e);
    }
  }

  protected String composeDn(String... parts) {
    StringWriter resultDn = new StringWriter();
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if(part == null || part.length()==0) {
        continue;
      }
      if(part.endsWith(",")) {
        part = part.substring(part.length()-2, part.length()-1);
      }
      if(part.startsWith(",")) {
        part = part.substring(1);
      }
      String currentDn = resultDn.toString();
      if(!currentDn.endsWith(",") && currentDn.length()>0) {
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
    if(user.getId() == null) {
      return false;
    }
    return user.getId().equals(org.camunda.bpm.engine.impl.context.Context.getCommandContext().getAuthenticatedUserId());
  }

  protected boolean isAuthorized(Permission permission, Resource resource, String resourceId) {
    return !ldapConfiguration.isAuthorizationCheckEnabled() || org.camunda.bpm.engine.impl.context.Context.getCommandContext()
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

  @Override
  public TenantQuery createTenantQuery() {
    return new LdapTenantQuery(org.camunda.bpm.engine.impl.context.Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
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
