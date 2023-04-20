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

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
public class LdapUserQueryImpl extends UserQueryImpl {

  private static final long serialVersionUID = 1L;
  private final LdapConfiguration ldapConfiguration;

  public LdapUserQueryImpl(LdapConfiguration ldapConfiguration) {
    super();
    this.ldapConfiguration = ldapConfiguration;
  }

  public LdapUserQueryImpl(CommandExecutor commandExecutor, LdapConfiguration ldapConfiguration) {
    super(commandExecutor);
    this.ldapConfiguration = ldapConfiguration;
  }

  // execute queries /////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    final LdapIdentityProviderSession provider = getLdapIdentityProvider(commandContext);
    return provider.findUserCountByQueryCriteria(this);
  }

  public List<User> executeList(CommandContext commandContext, Page page) {
    final LdapIdentityProviderSession provider = getLdapIdentityProvider(commandContext);
    return provider.findUserByQueryCriteria(this);
  }

  protected LdapIdentityProviderSession getLdapIdentityProvider(CommandContext commandContext) {
    return (LdapIdentityProviderSession) commandContext.getReadOnlyIdentityProvider();
  }

  @Override
  public UserQuery desc() {
    // provide this exception then a popup will be visible in the admin task, but display will run correctly
    if (ldapConfiguration != null && !ldapConfiguration.isSortControlSupported()) {
      throw new UnsupportedOperationException("The LDAP identity provider does not support descending search order.");
    }

    return this;
  }

}
