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
package org.camunda.bpm.engine.impl.identity;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Allows to expose the id of the currently authenticated user,
 * his groups and his tenants to the process engine.</p>
 *
 * <p>The current authentication is managed using a Thread Local. The value can
 * be set using {@link #setCurrentAuthentication(String, List)},
 * retrieved using {@link #getCurrentAuthentication()} and cleared
 * using {@link #clearCurrentAuthentication()}.</p>
 *
 * <p>Users typically do not use this class directly but rather use
 * the corresponding Service API methods:
 * <ul>
 * <li></li>
 * </ul>
 * </p>
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class Authentication {

  protected String authenticatedUserId;
  protected List<String> authenticatedGroupIds;
  protected List<String> authenticatedTenantIds;

  public Authentication() {
  }

  public Authentication(String authenticatedUserId, List<String> groupIds) {
    this(authenticatedUserId, groupIds, null);
  }

  public Authentication(String authenticatedUserId, List<String> authenticatedGroupIds, List<String> authenticatedTenantIds) {
    this.authenticatedUserId = authenticatedUserId;

    if (authenticatedGroupIds != null) {
      this.authenticatedGroupIds = new ArrayList<String>(authenticatedGroupIds);
    }

    if (authenticatedTenantIds != null) {
      this.authenticatedTenantIds = new ArrayList<String>(authenticatedTenantIds);
    }
  }

  public List<String> getGroupIds() {
    return authenticatedGroupIds;
  }

  public String getUserId() {
    return authenticatedUserId;
  }

  public List<String> getTenantIds() {
    return authenticatedTenantIds;
  }

}
