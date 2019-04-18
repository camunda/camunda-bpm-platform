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
package org.camunda.bpm.engine.impl.db;

import org.camunda.bpm.engine.impl.QueryOrderingProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Meyer
 */
public class ListQueryParameterObject implements Serializable {

  private static final long serialVersionUID = 1L;

  protected AuthorizationCheck authCheck = new AuthorizationCheck();

  protected TenantCheck tenantCheck = new TenantCheck();
  protected List<QueryOrderingProperty> orderingProperties = new ArrayList<QueryOrderingProperty>();

  protected int maxResults = Integer.MAX_VALUE;
  protected int firstResult = 0;
  protected Object parameter;
  protected String databaseType;

  public ListQueryParameterObject() {
  }

  public ListQueryParameterObject(Object parameter, int firstResult, int maxResults) {
    this.parameter = parameter;
    this.firstResult = firstResult;
    this.maxResults = maxResults;
  }

  public int getFirstResult() {
    return firstResult;
  }

  public int getFirstRow() {
    return firstResult +1;
  }

  public int getLastRow() {
    if(maxResults == Integer.MAX_VALUE) {
      return maxResults;
    }
    return  firstResult + maxResults + 1;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public Object getParameter() {
    return parameter;
  }

  public void setFirstResult(int firstResult) {
    this.firstResult = firstResult;
  }

  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  public void setParameter(Object parameter) {
    this.parameter = parameter;
  }

  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
  }

  public String getDatabaseType() {
    return databaseType;
  }

  public AuthorizationCheck getAuthCheck() {
    return authCheck;
  }

  public void setAuthCheck(AuthorizationCheck authCheck) {
    this.authCheck = authCheck;
  }

  public TenantCheck getTenantCheck() {
    return tenantCheck;
  }

  public void setTenantCheck(TenantCheck tenantCheck) {
    this.tenantCheck = tenantCheck;
  }

  public List<QueryOrderingProperty> getOrderingProperties() {
    return orderingProperties;
  }

  public void setOrderingProperties(List<QueryOrderingProperty> orderingProperties) {
    this.orderingProperties = orderingProperties;
  }
}
