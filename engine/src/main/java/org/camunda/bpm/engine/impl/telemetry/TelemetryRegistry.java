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
package org.camunda.bpm.engine.impl.telemetry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServerImpl;
import org.camunda.bpm.engine.impl.telemetry.dto.LicenseKeyDataImpl;

public class TelemetryRegistry {

  protected Map<String, CommandCounter> commands = new HashMap<>();
  protected ApplicationServerImpl applicationServer;
  protected LicenseKeyDataImpl licenseKey;
  protected String camundaIntegration;
  protected Set<String> webapps = new HashSet<>();

  // keeps track of the local telemetry activation state
  // note that in the database the value may have already changed
  // by another process engine, so this doesn't have to be in sync
  protected AtomicBoolean telemetryLocallyActivated = new AtomicBoolean(false);

  public synchronized ApplicationServerImpl getApplicationServer() {
    if (applicationServer == null) {
      applicationServer = PlatformTelemetryRegistry.getApplicationServer();
    }
    return applicationServer;
  }

  public synchronized void setApplicationServer(String applicationServerVersion) {
    this.applicationServer = new ApplicationServerImpl(applicationServerVersion);
  }

  public Map<String, CommandCounter> getCommands() {
    return commands;
  }

  public String getCamundaIntegration() {
    return camundaIntegration;
  }

  public void setCamundaIntegration(String camundaIntegration) {
    this.camundaIntegration = camundaIntegration;
  }

  public LicenseKeyDataImpl getLicenseKey() {
    return licenseKey;
  }

  public void setLicenseKey(LicenseKeyDataImpl licenseKey) {
    this.licenseKey = licenseKey;
  }

  public synchronized Set<String> getWebapps() {
    return webapps;
  }

  public synchronized void setWebapps(Set<String> webapps) {
    this.webapps = webapps;
  }

  public void markOccurrence(String name) {
    markOccurrence(name, 1);
  }

  public void markOccurrence(String name, long times) {
    CommandCounter counter = commands.get(name);
    if (counter == null) {
      synchronized (commands) {
        if (counter == null) {
          counter = new CommandCounter(name);
          commands.put(name, counter);
        }
      }
    }

    counter.mark(times);
  }

  public synchronized void addWebapp(String webapp) {
    if (!webapps.contains(webapp)) {
      webapps.add(webapp);
    }
  }

  public boolean isTelemetryLocallyActivated() {
    return telemetryLocallyActivated.get();
  }

  /**
   * @return previous value
   */
  public boolean setTelemetryLocallyActivated(boolean activated) {
    return this.telemetryLocallyActivated.getAndSet(activated);
  }

  public void clearCommandCounts() {
    commands.clear();
  }

  public void clear() {
    commands.clear();
    licenseKey = null;
    applicationServer = null;
    webapps.clear();
  }

}
