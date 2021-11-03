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

import java.util.Map;

/**
 * This class represents the data structure used for collecting information
 * about the license key issued for enterprise versions of Camunda Platform.
 *
 * This information is sent to Camunda when telemetry is enabled.
 *
 * @see <a href=
 *      "https://docs.camunda.org/manual/latest/introduction/telemetry/#collected-data">Camunda
 *      Documentation: Collected Telemetry Data</a>
 */
public interface LicenseKeyData {

  /**
   * The name of the customer this license was issued for.
   */
  public String getCustomer();

  /**
   * Camunda uses different license types e.g., when one license includes usage
   * of Cawemo enterprise.
   */
  public String getType();

  /**
   * The expiry date of the license in the format 'YYYY-MM-DD'.
   */
  public String getValidUntil();

  /**
   * A flag indicating if the license is unlimited or expires.
   */
  public Boolean isUnlimited();

  /**
   * A collection of features that are enabled through this license. Features
   * could be Camunda BPM, Optimize or Cawemo.
   */
  public Map<String, String> getFeatures();

  /**
   * The raw license data. This combines all data fields also included in this
   * class in the form which is stored in the license key String. Note, that
   * this is not the license key as issued to the customer but only contains the
   * plain-text part of it and not the encrypted key.
   */
  public String getRaw();
}
