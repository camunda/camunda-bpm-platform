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
package org.camunda.bpm.engine.impl.telemetry.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

public class Internals {

  public static final String SERIALIZED_APPLICATION_SERVER = "application-server";
  public static final String SERIALIZED_CAMUNDA_INTEGRATION = "camunda-integration";
  public static final String SERIALIZED_LICENSE_KEY = "license-key";
  public static final String SERIALIZED_TELEMETRY_ENABLED = "telemetry-enabled";

  protected Database database;
  @SerializedName(value = SERIALIZED_APPLICATION_SERVER)
  protected ApplicationServer applicationServer;
  @SerializedName(value = SERIALIZED_LICENSE_KEY)
  protected LicenseKeyData licenseKey;
  protected Map<String, Command> commands;
  @SerializedName(value = SERIALIZED_CAMUNDA_INTEGRATION)
  protected Set<String> camundaIntegration;

  protected Map<String, Metric> metrics;
  protected Set<String> webapps;

  protected Jdk jdk;

  @SerializedName(value = SERIALIZED_TELEMETRY_ENABLED)
  protected Boolean telemetryEnabled;

  public Internals() {
    this(null, null, null, null);
  }

  public Internals(Database database, ApplicationServer server, LicenseKeyData licenseKey, Jdk jdk) {
    this.database = database;
    this.applicationServer = server;
    this.licenseKey = licenseKey;
    this.commands = new HashMap<>();
    this.jdk = jdk;
    this.camundaIntegration = new HashSet<>();
  }

  public Internals(Internals internals) {
    this(internals.database, internals.applicationServer, internals.licenseKey, internals.jdk);
    this.camundaIntegration = internals.camundaIntegration == null ? null : new HashSet<>(internals.getCamundaIntegration());
    this.commands = new HashMap<>(internals.getCommands());
    this.metrics = internals.metrics == null ? null : new HashMap<>(internals.getMetrics());
    this.telemetryEnabled = internals.telemetryEnabled;
    this.webapps = internals.webapps;
  }

  public Database getDatabase() {
    return database;
  }

  public void setDatabase(Database database) {
    this.database = database;
  }

  public ApplicationServer getApplicationServer() {
    return applicationServer;
  }

  public void setApplicationServer(ApplicationServer applicationServer) {
    this.applicationServer = applicationServer;
  }

  public Map<String, Command> getCommands() {
    return commands;
  }

  public void setCommands(Map<String, Command> commands) {
    this.commands = commands;
  }

  public Map<String, Metric> getMetrics() {
    return metrics;
  }

  public void setMetrics(Map<String, Metric> metrics) {
    this.metrics = metrics;
  }

  public void mergeDynamicData(Internals other) {
    this.commands = other.commands;
    this.metrics = other.metrics;
  }

  public Jdk getJdk() {
    return jdk;
  }

  public void setJdk(Jdk jdk) {
    this.jdk = jdk;
  }

  public Set<String> getCamundaIntegration() {
    return camundaIntegration;
  }

  public void setCamundaIntegration(Set<String> camundaIntegration) {
    this.camundaIntegration = camundaIntegration;
  }

  public LicenseKeyData getLicenseKey() {
    return licenseKey;
  }

  public void setLicenseKey(LicenseKeyData licenseKey) {
    this.licenseKey = licenseKey;
  }

  public Boolean getTelemetryEnabled() {
    return telemetryEnabled;
  }

  public void setTelemetryEnabled(Boolean telemetryEnabled) {
    this.telemetryEnabled = telemetryEnabled;
  }

  public Set<String> getWebapps() {
    return webapps;
  }

  public void setWebapps(Set<String> webapps) {
    this.webapps = webapps;
  }

}
