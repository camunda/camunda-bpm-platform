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
package org.camunda.bpm.identity.impl.ldap.util;

import javax.naming.directory.SearchResult;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;
import org.camunda.commons.logging.BaseLogger;

/**
 * @author Thorben Lindhauer
 *
 */
public class LdapPluginLogger extends BaseLogger {

  public static final String PROJECT_CODE = "LDAP";

  public static final LdapPluginLogger INSTANCE = BaseLogger.createLogger(
      LdapPluginLogger.class, PROJECT_CODE, "org.camunda.bpm.identity.impl.ldap", "00");

  public void pluginActivated(String pluginClassName, String engineName) {
    logInfo("001", "PLUGIN {} activated on process engine {}", pluginClassName, engineName);
  }

  public void acceptingUntrustedCertificates() {
    logWarn("002", "Enabling accept of untrusted certificates. Use at own risk.");
  }

  public void exceptionWhenClosingLdapContext(Exception e) {
    logDebug("003", "exception while closing LDAP DIR CTX", e);
  }

  public <E extends DbEntity> void invalidLdapEntityReturned(E entity, SearchResult searchResult) {
    String entityType = entity instanceof UserEntity ? "user" : "group";
    logError("004", "LDAP {} query returned a {} with id null. This {} will be ignored. "
        + "This indicates a misconfiguration of the LDAP plugin or a problem with the LDAP service."
        + " Enable DEBUG/FINE logging for details.", entityType, entityType, entityType);
    // log sensitive data only on FINE
    logDebug("004", "Invalid {}: {} based on search result {}", entityType, entity, searchResult);
  }

  public void queryResult(String summary) {
    // log sensitive data only on FINE
    logDebug("005", summary);
  }
}
