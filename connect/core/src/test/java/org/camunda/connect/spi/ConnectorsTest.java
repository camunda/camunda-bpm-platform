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
package org.camunda.connect.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.camunda.connect.Connectors;
import org.camunda.connect.dummy.DummyConnector;
import org.junit.Test;

public class ConnectorsTest {

  @Test
  public void shouldReturnNullForUnknownConnectorId() {
    Connector unknown = Connectors.getConnector("unknown");
    assertThat(unknown).isNull();
  }

  @Test
  public void shouldMyHttpConnector() {
    Connector http = Connectors.http();
    assertThat(http).isNotNull();
  }

  @Test
  public void shouldNotDiscoverSoapConnector() {
    Connector soap = Connectors.soap();
    assertThat(soap).isNull();
  }

  @Test
  public void shouldDiscoverDummyConnector() {
    DummyConnector connector = Connectors.getConnector(DummyConnector.ID);
    assertThat(connector).isNotNull();

    assertThat(Connectors.getAvailableConnectors())
      .hasSize(2)
      .contains(connector);
  }

  @Test
  public void shouldConfigureDummyConnector() {
    DummyConnector connector = new DummyConnector();
    assertThat(connector.getConfiguration()).isEqualTo("default");

    connector = Connectors.getConnector(DummyConnector.ID);
    assertThat(connector.getConfiguration()).isEqualTo("configured");
  }

}
