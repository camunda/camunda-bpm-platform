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
package org.camunda.bpm.engine.telemetry;

import org.camunda.bpm.engine.ManagementService;

/**
 * The engine collects information about multiple aspects of the installation.
 *
 * If telemetry is enabled this information is sent to Camunda. If telemetry is
 * disabled, the engine still collects this information and provides it through
 * {@link ManagementService#getTelemetryData()}.
 *
 * This class represents the data structure used to collect telemetry data.
 *
 * @see <a href=
 *      "https://docs.camunda.org/manual/latest/introduction/telemetry/#collected-data">Camunda
 *      Documentation: Collected Telemetry Data</a>
 */
public interface TelemetryData {

  /**
   * This method returns a String which is unique for each installation of
   * Camunda. It is stored once per database so all engines connected to the
   * same database will have the same installation ID. The ID is used to
   * identify a single installation of Camunda Platform.
   */
  public String getInstallation();

  /**
   * Returns a data object that stores information about the used Camunda
   * product.
   */
  public Product getProduct();
}
