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
package org.camunda.bpm.engine.impl.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsRegistry {

  protected Map<String, Meter> dbMeters = new HashMap<>();
  protected Map<String, Meter> telemetryMeters = new HashMap<>();

  public Meter getDbMeterByName(String name) {
    return dbMeters.get(name);
  }

  public Map<String, Meter> getDbMeters() {
    return dbMeters;
  }

  public Map<String, Meter> getTelemetryMeters() {
    return telemetryMeters;
  }

  public void clearTelemetryMetrics() {
    telemetryMeters.values().forEach(Meter::getAndClear);
  }

  public void markOccurrence(String name) {
    markOccurrence(name, 1);
  }

  public void markOccurrence(String name, long times) {
    markOccurrence(dbMeters, name, times);
    markOccurrence(telemetryMeters, name, times);
  }

  public void markTelemetryOccurrence(String name, long times) {
    markOccurrence(telemetryMeters, name, times);
  }

  protected void markOccurrence(Map<String, Meter> meters, String name, long times) {
    Meter meter = meters.get(name);

    if (meter != null) {
      meter.markTimes(times);
    }
  }

  /**
   * Creates a meter for both database and telemetry collection.
   */
  public void createMeter(String name) {
    Meter dbMeter = new Meter(name);
    dbMeters.put(name, dbMeter);

    Meter telemetryMeter = new Meter(name);
    telemetryMeters.put(name, telemetryMeter);
  }

  /**
   * Creates a meter only for database collection.
   */
  public void createDbMeter(String name) {
    Meter dbMeter = new Meter(name);
    dbMeters.put(name, dbMeter);
  }
}
