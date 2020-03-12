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
package org.camunda.bpm.spring.boot.starter.runlistener;

import org.camunda.bpm.spring.boot.starter.util.CamundaBpmVersion;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Adds camunda.bpm.version properties to environment.
 */
public class PropertiesListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  private final CamundaBpmVersion version;

  /**
   * Default constructor, used when initializing via spring.factories.
   *
   * @see PropertiesListener#PropertiesListener(CamundaBpmVersion)
   */
  public PropertiesListener() {
    this(new CamundaBpmVersion());
  }

  /**
   * Initialize with version.
   *
   * @param version the current camundaBpmVersion instance.
   */
  PropertiesListener(CamundaBpmVersion version) {
    this.version = version;
  }

  @Override
  public void onApplicationEvent(final ApplicationEnvironmentPreparedEvent event) {
    event.getEnvironment()
      .getPropertySources()
      .addFirst(version.getPropertiesPropertySource());
  }

}
