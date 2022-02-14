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
package org.camunda.bpm.run.property;

import org.camunda.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin;

public class CamundaBpmRunAdministratorAuthorizationProperties extends AdministratorAuthorizationPlugin {

  public static final String PREFIX = CamundaBpmRunProperties.PREFIX + ".admin-auth";

  boolean enabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return "CamundaBpmRunAdministratorAuthorizationProperty [enabled=" + enabled +
        ", administratorGroupName=" + administratorGroupName +
        ", administratorUserName=" + administratorUserName +
        ']';
  }
}