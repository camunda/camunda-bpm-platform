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
package org.camunda.bpm.spring.boot.starter.configuration.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class DefaultDeploymentConfigurationTest {

  private final DefaultDeploymentConfiguration defaultDeploymentConfiguration = new DefaultDeploymentConfiguration();
  private final CamundaBpmProperties camundaBpmProperties = new CamundaBpmProperties();
  private final SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();

  @Before
  public void before() {
    defaultDeploymentConfiguration.camundaBpmProperties = camundaBpmProperties;
  }

  @Test
  public void noDeploymentTest() {
    camundaBpmProperties.setAutoDeploymentEnabled(false);
    defaultDeploymentConfiguration.preInit(configuration);

    assertThat(configuration.getDeploymentResources()).isEmpty();
  }

  @Test
  public void deploymentTest() throws IOException {
    camundaBpmProperties.setAutoDeploymentEnabled(true);
    defaultDeploymentConfiguration.preInit(configuration);

    final Resource[] resources = configuration.getDeploymentResources();
    assertThat(resources).hasSize(11);

    assertThat(filenames(resources)).containsOnly("async-service-task.bpmn", "test.cmmn10.xml",
      "test.bpmn", "test.cmmn", "test.bpmn20.xml", "check-order.dmn", "eventing.bpmn",
      "spin-java8-model.bpmn", "eventingWithTaskAssignee.bpmn","eventingWithBoundary.bpmn",
      "eventingWithIntermediateCatch.bpmn");
  }

  private Set<String> filenames(Resource[] resources) {
    Set<String> filenames = new HashSet<String>();
    for (Resource resource : resources) {
      filenames.add(resource.getFilename());
    }
    return filenames;
  }
}
