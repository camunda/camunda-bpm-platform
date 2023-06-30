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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

public class LdapSearchResults implements NamingEnumeration<SearchResult>, AutoCloseable {

  protected NamingEnumeration<SearchResult> enumeration;

  public LdapSearchResults(NamingEnumeration<SearchResult> enumeration) {
    this.enumeration = enumeration;
  }

  @Override
  public SearchResult next() throws NamingException {
    return enumeration.next();
  }

  @Override
  public boolean hasMore() throws NamingException {
    return enumeration.hasMore();
  }

  @Override
  public boolean hasMoreElements() {
    return enumeration.hasMoreElements();
  }

  @Override
  public SearchResult nextElement() {
    return enumeration.nextElement();
  }

  @Override
  public void close() {
    try {
      if (enumeration != null) {
        enumeration.close();
      }
    } catch (Exception e) {
      // ignore silently
    }
  }

}
