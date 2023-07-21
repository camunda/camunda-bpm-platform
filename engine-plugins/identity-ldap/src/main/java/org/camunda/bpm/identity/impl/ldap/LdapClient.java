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

import org.camunda.bpm.engine.impl.identity.IdentityProviderException;
import org.camunda.bpm.identity.impl.ldap.util.LdapPluginLogger;

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
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This wrapper class should ensure that LDAP exceptions are wrapped as process engine exceptions
 * to avoid that error details are disclosed in the REST API.
 */
public class LdapClient {

  protected LdapContext initialContext;
  protected LdapConfiguration ldapConfiguration;

  public LdapClient(LdapConfiguration ldapConfiguration) {
    this.ldapConfiguration = ldapConfiguration;
  }

  protected void ensureContextInitialized() {
    if (initialContext == null) {
      initialContext = openContext();
    }
  }

  public LdapContext openContext(String dn, String password) {
    Hashtable<String, String> env = new Hashtable<>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfiguration.getInitialContextFactory());
    env.put(Context.SECURITY_AUTHENTICATION, ldapConfiguration.getSecurityAuthentication());
    env.put(Context.PROVIDER_URL, ldapConfiguration.getServerUrl());
    env.put(Context.SECURITY_PRINCIPAL, dn);
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

  protected LdapContext openContext() {
    return openContext(ldapConfiguration.getManagerDn(), ldapConfiguration.getManagerPassword());
  }

  protected void closeLdapCtx() {
    closeLdapCtx(initialContext);
  }

  protected void closeLdapCtx(LdapContext context) {
    if (context != null) {
      try {
        context.close();
      } catch (NamingException e) {
        // ignore
        LdapPluginLogger.INSTANCE.exceptionWhenClosingLdapContext(e);
      }
    }
  }

  public LdapSearchResults search(String baseDn, String searchFilter) {
    try {
      return new LdapSearchResults(initialContext.search(baseDn, searchFilter, ldapConfiguration.getSearchControls()));
    } catch (NamingException e) {
      throw new IdentityProviderException("LDAP search request failed.", e);
    }
  }

  public void setRequestControls(List<Control> listControls) {
    try {
      initialContext.setRequestControls(listControls.toArray(new Control[0]));
    } catch (NamingException e) {
      throw new IdentityProviderException("LDAP server failed to set request controls.", e);
    }
  }

  public Control[] getResponseControls() {
    try {
      return initialContext.getResponseControls();
    } catch (NamingException e) {
      throw new IdentityProviderException("Error occurred while getting the response controls from the LDAP server.", e);
    }
  }

  public static void addPaginationControl(List<Control> listControls, byte[] cookie, Integer pageSize) {
    try {
      listControls.add(new PagedResultsControl(pageSize, cookie, Control.NONCRITICAL));
    } catch (IOException e) {
      throw new IdentityProviderException("Pagination couldn't be enabled.", e);
    }
  }

  public static void addSortKey(SortKey sortKey, List<Control> controls) {
    try {
      controls.add(new SortControl(new SortKey[] { sortKey }, Control.CRITICAL));
    } catch (IOException e) {
      throw new IdentityProviderException("Sorting couldn't be enabled.", e);
    }
  }

  protected static String getValue(String attrName, Attributes attributes) {
    Attribute attribute = attributes.get(attrName);
    if (attribute != null) {
      try {
        return (String) attribute.get();
      } catch (NamingException e) {
        throw new IdentityProviderException("Error occurred while retrieving the value.", e);
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static NamingEnumeration<String> getAllMembers(String attributeId, LdapSearchResults searchResults) {
    SearchResult result = searchResults.nextElement();
    Attributes attributes = result.getAttributes();
    if (attributes != null) {
      Attribute memberAttribute = attributes.get(attributeId);
      if (memberAttribute != null) {
        try {
          return (NamingEnumeration<String>) memberAttribute.getAll();
        } catch (NamingException e) {
          throw new IdentityProviderException("Value couldn't be retrieved.", e);
        }
      }
    }

    return null;
  }

}
