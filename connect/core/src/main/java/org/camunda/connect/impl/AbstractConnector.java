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
package org.camunda.connect.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorRequest;
import org.camunda.connect.spi.ConnectorResponse;
import org.camunda.connect.spi.ConnectorRequestInterceptor;

/**
 * Abstract implementation of the connector interface.
 *
 * This implementation provides a linked list of interceptors and related methods for
 * handling interceptor invocation.
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractConnector<Q extends ConnectorRequest<R>, R extends ConnectorResponse> implements Connector<Q> {

  protected String connectorId;

  /**
   * The {@link ConnectorRequestInterceptor} chain
   */
  protected List<ConnectorRequestInterceptor> requestInterceptors = new LinkedList<ConnectorRequestInterceptor>();

  public AbstractConnector(String connectorId) {
    this.connectorId = connectorId;
  }

  public String getId() {
    return connectorId;
  }

  public List<ConnectorRequestInterceptor> getRequestInterceptors() {
    return requestInterceptors;
  }

  public void setRequestInterceptors(List<ConnectorRequestInterceptor> requestInterceptors) {
    this.requestInterceptors = requestInterceptors;
  }

  public Connector<Q> addRequestInterceptor(ConnectorRequestInterceptor interceptor) {
    requestInterceptors.add(interceptor);
    return this;
  }

  public Connector<Q> addRequestInterceptors(Collection<ConnectorRequestInterceptor> interceptors) {
    requestInterceptors.addAll(interceptors);
    return this;
  }

}
