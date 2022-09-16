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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.telemetry.Command;
import org.camunda.bpm.engine.telemetry.Internals;
import org.camunda.bpm.engine.telemetry.Metric;

import com.google.gson.annotations.SerializedName;

public class InternalsImpl implements Internals {

  public static final String SERIALIZED_APPLICATION_SERVER = "application-server";
  public static final String SERIALIZED_CAMUNDA_INTEGRATION = "camunda-integration";
  public static final String SERIALIZED_LICENSE_KEY = "license-key";
  public static final String SERIALIZED_TELEMETRY_ENABLED = "telemetry-enabled";
  public static final String SERIALIZED_TELEMETRY_DATA_COLLECTION_START_DATE = "data-collection-start-date";

  protected DatabaseImpl database;
  @SerializedName(value = SERIALIZED_APPLICATION_SERVER)
  protected ApplicationServerImpl applicationServer;
  @SerializedName(value = SERIALIZED_LICENSE_KEY)
  protected LicenseKeyDataImpl licenseKey;
  @SerializedName(value = SERIALIZED_CAMUNDA_INTEGRATION)
  protected Set<String> camundaIntegration;
  @SerializedName(value = SERIALIZED_TELEMETRY_DATA_COLLECTION_START_DATE)
  protected Date dataCollectionStartDate;
  protected Map<String, Command> commands;
  protected Map<String, Metric> metrics;
  protected Set<String> webapps;

  protected JdkImpl jdk;

  @SerializedName(value = SERIALIZED_TELEMETRY_ENABLED)
  protected Boolean telemetryEnabled;

  public InternalsImpl() {
    this(null, null, null, null);
  }

  public InternalsImpl(DatabaseImpl database, ApplicationServerImpl server, LicenseKeyDataImpl licenseKey, JdkImpl jdk) {
    this.database = database;
    this.applicationServer = server;
    this.licenseKey = licenseKey;
    this.commands = new HashMap<>();
    this.jdk = jdk;
    this.camundaIntegration = new HashSet<>();
  }

  public InternalsImpl(InternalsImpl internals) {
    this(internals.database, internals.applicationServer, internals.licenseKey, internals.jdk);
    this.camundaIntegration = internals.camundaIntegration == null ? null : new HashSet<>(internals.getCamundaIntegration());
    this.commands = new HashMap<>(internals.getCommands());
    this.metrics = internals.metrics == null ? null : new HashMap<>(internals.getMetrics());
    this.telemetryEnabled = internals.telemetryEnabled;
    this.webapps = internals.webapps;
    this.dataCollectionStartDate = internals.dataCollectionStartDate;
  }

  public DatabaseImpl getDatabase() {
    return database;
  }

  public void setDatabase(DatabaseImpl database) {
    this.database = database;
  }

  public ApplicationServerImpl getApplicationServer() {
    return applicationServer;
  }

  public void setApplicationServer(ApplicationServerImpl applicationServer) {
    this.applicationServer = applicationServer;
  }

  @Override
  public Date getDataCollectionStartDate() {
    return dataCollectionStartDate;
  }

  public void setDataCollectionStartDate(Date dataCollectionStartDate) {
    this.dataCollectionStartDate = dataCollectionStartDate;
  }

  public Map<String, Command> getCommands() {
    return commands;
  }

  public void setCommands(Map<String, Command> commands) {
    this.commands = commands;
  }

  public void putCommand(String commandName, int count) {
    if (commands == null) {
      commands = new HashMap<>();
    }

    commands.put(commandName, new CommandImpl(count));
  }

  public Map<String, Metric> getMetrics() {
    return metrics;
  }

  public void setMetrics(Map<String, Metric> metrics) {
    this.metrics = metrics;
  }

  public void putMetric(String metricName, int count) {
    if (metrics == null) {
      metrics = new HashMap<>();
    }

    metrics.put(metricName, new MetricImpl(count));
  }

  public void mergeDynamicData(InternalsImpl other) {
    this.commands = other.commands;
    this.metrics = other.metrics;
  }

  public JdkImpl getJdk() {
    return jdk;
  }

  public void setJdk(JdkImpl jdk) {
    this.jdk = jdk;
  }

  public Set<String> getCamundaIntegration() {
    return camundaIntegration;
  }

  public void setCamundaIntegration(Set<String> camundaIntegration) {
    this.camundaIntegration = camundaIntegration;
  }

  public LicenseKeyDataImpl getLicenseKey() {
    return licenseKey;
  }

  public void setLicenseKey(LicenseKeyDataImpl licenseKey) {
    this.licenseKey = licenseKey;
  }

  public Boolean isTelemetryEnabled() {
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
