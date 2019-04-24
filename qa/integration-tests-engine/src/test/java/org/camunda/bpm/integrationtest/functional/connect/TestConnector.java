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
package org.camunda.bpm.integrationtest.functional.connect;

import java.util.Collection;
import java.util.List;

import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorRequestInterceptor;
import org.camunda.connect.spi.ConnectorResponse;

public class TestConnector implements Connector<TestConnectorRequest> {

  public static final String ID = "pa-test-connector";

  public String getId() {
    return ID;
  }

  public TestConnectorRequest createRequest() {
    return new TestConnectorRequest();
  }

  public List<ConnectorRequestInterceptor> getRequestInterceptors() {
    return null;
  }

  public void setRequestInterceptors(List<ConnectorRequestInterceptor> list) {
    // ignore
  }

  public Connector<TestConnectorRequest> addRequestInterceptor(ConnectorRequestInterceptor connectorRequestInterceptor) {
    // ignore
    return this;
  }

  public Connector<TestConnectorRequest> addRequestInterceptors(Collection<ConnectorRequestInterceptor> collection) {
    // ignore
    return this;
  }

  public ConnectorResponse execute(TestConnectorRequest testConnectorRequest) {
    return testConnectorRequest.execute();
  }

}
