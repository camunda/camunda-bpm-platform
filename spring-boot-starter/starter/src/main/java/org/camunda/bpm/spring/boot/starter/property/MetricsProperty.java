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
package org.camunda.bpm.spring.boot.starter.property;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties.joinOn;

public class MetricsProperty {

  private boolean enabled = Defaults.INSTANCE.isMetricsEnabled();
  private boolean dbReporterActivate = Defaults.INSTANCE.isDbMetricsReporterActivate();

  @NestedConfigurationProperty
  private ActuatorProperty actuator = new ActuatorProperty();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isDbReporterActivate() {
    return dbReporterActivate;
  }

  public void setDbReporterActivate(boolean dbReporterActivate) {
    this.dbReporterActivate = dbReporterActivate;
  }

  public ActuatorProperty getActuator() {
    return actuator;
  }

  public void setActuator(ActuatorProperty actuator) {
    this.actuator = actuator;
  }

  @Override
  public String toString() {
    return joinOn(this.getClass())
      .add("enabled=" + enabled)
      .add("dbReporterActivate=" + dbReporterActivate)
      .add("actuator=" + actuator)
      .toString();
  }

}
