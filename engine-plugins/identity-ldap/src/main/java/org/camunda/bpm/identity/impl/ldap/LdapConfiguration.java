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


import javax.naming.directory.SearchControls;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>Java Bean holding LDAP configuration</p>
 *
 * @author Daniel Meyer
 *
 */
public class LdapConfiguration {

  protected String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
  protected String securityAuthentication = "simple";

  protected Map<String, String> contextProperties = new HashMap<String, String>();

  protected String serverUrl;
  protected String managerDn = "";
  protected String managerPassword = "";

  protected String baseDn = "";

  protected String userDnPattern = "";

  protected String userSearchBase = "";
  protected String userSearchFilter = "(objectclass=person)";

  protected String groupSearchBase = "";
  protected String groupSearchFilter = "(objectclass=groupOfNames)";

  protected String userIdAttribute = "uid";
  protected String userFirstnameAttribute = "cn";
  protected String userLastnameAttribute = "sn";
  protected String userEmailAttribute = "email";
  protected String userPasswordAttribute = "userpassword";

  protected String groupIdAttribute = "ou";
  protected String groupNameAttribute = "cn";
  protected String groupTypeAttribute = "";
  protected String groupMemberAttribute = "memberOf";

  protected boolean sortControlSupported = false;
  protected boolean useSsl = false;
  protected boolean usePosixGroups = false;
  protected boolean allowAnonymousLogin = false;

  protected boolean authorizationCheckEnabled = true;

  // getters / setters //////////////////////////////////////

  public String getInitialContextFactory() {
    return initialContextFactory;
  }

  public void setInitialContextFactory(String initialContextFactory) {
    this.initialContextFactory = initialContextFactory;
  }

  public String getSecurityAuthentication() {
    return securityAuthentication;
  }

  public void setSecurityAuthentication(String securityAuthentication) {
    this.securityAuthentication = securityAuthentication;
  }

  public Map<String, String> getContextProperties() {
    return contextProperties;
  }

  public void setContextProperties(Map<String, String> contextProperties) {
    this.contextProperties = contextProperties;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public String getManagerDn() {
    return managerDn;
  }

  public void setManagerDn(String managerDn) {
    this.managerDn = managerDn;
  }

  public String getManagerPassword() {
    return managerPassword;
  }

  public void setManagerPassword(String managerPassword) {
    this.managerPassword = managerPassword;
  }

  public String getUserDnPattern() {
    return userDnPattern;
  }

  public void setUserDnPattern(String userDnPattern) {
    this.userDnPattern = userDnPattern;
  }

  public String getGroupSearchBase() {
    return groupSearchBase;
  }

  public void setGroupSearchBase(String groupSearchBase) {
    this.groupSearchBase = groupSearchBase;
  }

  public String getGroupSearchFilter() {
    return groupSearchFilter;
  }

  public void setGroupSearchFilter(String groupSearchFilter) {
    this.groupSearchFilter = groupSearchFilter;
  }

  public String getGroupNameAttribute() {
    return groupNameAttribute;
  }

  public void setGroupNameAttribute(String groupNameAttribute) {
    this.groupNameAttribute = groupNameAttribute;
  }

  public String getBaseDn() {
    return baseDn;
  }

  public void setBaseDn(String baseDn) {
    this.baseDn = baseDn;
  }

  public String getUserSearchBase() {
    return userSearchBase;
  }

  public void setUserSearchBase(String userSearchBase) {
    this.userSearchBase = userSearchBase;
  }

  public String getUserSearchFilter() {
    return userSearchFilter;
  }

  public void setUserSearchFilter(String userSearchFilter) {
    this.userSearchFilter = userSearchFilter;
  }

  public String getUserIdAttribute() {
    return userIdAttribute;
  }

  public void setUserIdAttribute(String userIdAttribute) {
    this.userIdAttribute = userIdAttribute;
  }

  public String getUserFirstnameAttribute() {
    return userFirstnameAttribute;
  }

  public void setUserFirstnameAttribute(String userFirstnameAttribute) {
    this.userFirstnameAttribute = userFirstnameAttribute;
  }

  public String getUserLastnameAttribute() {
    return userLastnameAttribute;
  }

  public void setUserLastnameAttribute(String userLastnameAttribute) {
    this.userLastnameAttribute = userLastnameAttribute;
  }

  public String getUserEmailAttribute() {
    return userEmailAttribute;
  }

  public void setUserEmailAttribute(String userEmailAttribute) {
    this.userEmailAttribute = userEmailAttribute;
  }

  public String getUserPasswordAttribute() {
    return userPasswordAttribute;
  }

  public void setUserPasswordAttribute(String userPasswordAttribute) {
    this.userPasswordAttribute = userPasswordAttribute;
  }

  public boolean isSortControlSupported() {
    return sortControlSupported;
  }

  public void setSortControlSupported(boolean sortControlSupported) {
    this.sortControlSupported = sortControlSupported;
  }

  public String getGroupIdAttribute() {
    return groupIdAttribute;
  }

  public void setGroupIdAttribute(String groupIdAttribute) {
    this.groupIdAttribute = groupIdAttribute;
  }

  public String getGroupMemberAttribute() {
    return groupMemberAttribute;
  }

  public void setGroupMemberAttribute(String groupMemberAttribute) {
    this.groupMemberAttribute = groupMemberAttribute;
  }

  public boolean isUseSsl() {
    return useSsl;
  }

  public void setUseSsl(boolean useSsl) {
    this.useSsl = useSsl;
  }

  public boolean isUsePosixGroups() {
    return usePosixGroups;
  }

  public void setUsePosixGroups(boolean usePosixGroups) {
    this.usePosixGroups = usePosixGroups;
  }

  public SearchControls getSearchControls() {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setTimeLimit(30000);
    return searchControls;
  }

  public String getGroupTypeAttribute() {
    return groupTypeAttribute;
  }

  public void setGroupTypeAttribute(String groupTypeAttribute) {
    this.groupTypeAttribute = groupTypeAttribute;
  }

  public boolean isAllowAnonymousLogin() {
    return allowAnonymousLogin;
  }

  public void setAllowAnonymousLogin(boolean allowAnonymousLogin) {
    this.allowAnonymousLogin = allowAnonymousLogin;
  }

  public boolean isAuthorizationCheckEnabled() {
    return authorizationCheckEnabled;
  }

  public void setAuthorizationCheckEnabled(boolean authorizationCheckEnabled) {
    this.authorizationCheckEnabled = authorizationCheckEnabled;
  }

}
