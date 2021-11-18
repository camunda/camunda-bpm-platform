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
package org.camunda.bpm.engine.telemetry.dto;

import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.ManagementService;

/**
 * This class represents the structure of data describing Camunda internal
 * metrics and the technical environment in which Camunda is set-up.
 *
 * This information is sent to Camunda when telemetry is enabled.
 *
 * @see <a href=
 *      "https://docs.camunda.org/manual/latest/introduction/telemetry/#collected-data">Camunda
 *      Documentation: Collected Telemetry Data</a>
 */
public interface Internals {

  /**
   * Information about the connected database system.
   */
  public Database getDatabase();

  /**
   * Information about the application server Camunda is running on.
   */
  public ApplicationServer getApplicationServer();

  /**
   * Information about the Camunda license key issued for enterprise editions of
   * Camunda Platform.
   */
  public LicenseKeyData getLicenseKey();

  /**
   * Information about the number of command executions performed by the Camunda
   * engine. If telemetry sending is enabled, the number of executions per
   * command resets on sending the data to Camunda. Retrieving the data through
   * {@link ManagementService#getTelemetryData()} will not reset the count.
   */
  public Map<String, Command> getCommands();

  /**
   * A selection of metrics collected by the engine. Metrics included are:
   * <ul>
   *   <li>The number of root process instance executions started.</li>
   *   <li>The number of activity instances started or also known as flow node
   * instances.</li>
   *   <li>The number of executed decision instances.</li>
   *   <li>The number of executed decision elements.</li>
   * </ul>
   * The metrics reset on sending the data to Camunda. Retrieving the data
   * through {@link ManagementService#getTelemetryData()} will not reset the
   * count.
   */
  public Map<String, Metric> getMetrics();

  /**
   * Used Camunda integrations (e.g, Spring boot starter, Camunda Platform Run,
   * WildFly/JBoss subsystem or Camunda EJB service).
   */
  public Set<String> getCamundaIntegration();

  /**
   * Webapps enabled in the Camunda installation (e.g., cockpit, admin,
   * tasklist).
   */
  public Set<String> getWebapps();

  /**
   * Information about the installed Java runtime environment.
   */
  public Jdk getJdk();

  /**
   * Flag that indicates if sending of telemetry data was enabled.
   */
  public Boolean isTelemetryEnabled();
}
