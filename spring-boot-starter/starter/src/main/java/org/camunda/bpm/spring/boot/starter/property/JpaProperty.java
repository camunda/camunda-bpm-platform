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
package org.camunda.bpm.spring.boot.starter.property;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class JpaProperty {
  /**
   * enables JPA
   */
  private boolean enabled;

  /**
   * the JPA persistence unit name
   */
  private String persistenceUnitName;

  /**
   * close JPA entity manager
   */
  private boolean closeEntityManager = true;

  /**
   * handle transactions by JPA
   */
  private boolean handleTransaction = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getPersistenceUnitName() {
    return persistenceUnitName;
  }

  public void setPersistenceUnitName(String persistenceUnitName) {
    this.persistenceUnitName = persistenceUnitName;
  }

  public boolean isCloseEntityManager() {
    return closeEntityManager;
  }

  public void setCloseEntityManager(boolean closeEntityManager) {
    this.closeEntityManager = closeEntityManager;
  }

  public boolean isHandleTransaction() {
    return handleTransaction;
  }

  public void setHandleTransaction(boolean handleTransaction) {
    this.handleTransaction = handleTransaction;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enabled=" + enabled)
      .add("persistenceUnitName=" + persistenceUnitName)
      .add("closeEntityManager=" + closeEntityManager)
      .add("handleTransaction=" + handleTransaction)
      .toString();
  }

}
