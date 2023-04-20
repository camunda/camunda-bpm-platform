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

import java.util.Date;
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
   * Camunda Platform. May be null when no license is used.
   */
  public LicenseKeyData getLicenseKey();

  /**
   * The date when the engine started to collect dynamic data, such as command executions
   * and metrics. If telemetry sending is enabled, dynamic data resets on sending the data
   * to Camunda.
   *
   * This method returns a date that represents the date and time when the dynamic data collected
   * for telemetry is reset. Dynamic data and the date returned by this method are reset in three
   * cases:
   *
   * <ul>
   *   <li>At engine startup, the date is set to the current time, even if telemetry is disabled.
   *       It is then only used by the telemetry Query API that returns the currently collected
   *       data but sending telemetry to Camunda is disabled.</li>
   *   <li>When sending telemetry to Camunda is enabled after engine start via API (e.g.,
   *       {@link ManagementService#toggleTelemetry(boolean)}. This call causes the engine to wipe
   *       all dynamic data and therefore the collection date is reset to the current time.</li>
   *   <li>When sending telemetry to Camunda is enabled, after sending the data, all existing dynamic
   *       data is wiped and therefore the collection date is reset to the current time.</li>
   * </ul>
   *
   * @return A date that represents the start of the time frame where the current telemetry
   * data set was collected.
   */
  public Date getDataCollectionStartDate();

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

}
