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
 * This class represents the data structure used for collecting information
 * about certain internal metrics for telemetry data. A metric is a counter for
 * a certain action performed by the engine (e.g., start a root
 * process-instance).
 *
 * This information is sent to Camunda when telemetry is enabled.
 *
 * When used for telemetry data collection, all metric counts reset on sending
 * the data. Retrieval through {@link ManagementService#getTelemetryData()} will
 * not reset the counter. Some metrics are used for billing purposes in
 * enterprise setups.
 *
 * @see <a href=
 *      "https://docs.camunda.org/manual/latest/introduction/telemetry/#collected-data">Camunda
 *      Documentation: Collected Telemetry Data</a>
 */
public interface Metric {

  /**
   * The count of this metric i.e., how often did the engine perform the action
   * associated with this metric.
   */
  public long getCount();
}
