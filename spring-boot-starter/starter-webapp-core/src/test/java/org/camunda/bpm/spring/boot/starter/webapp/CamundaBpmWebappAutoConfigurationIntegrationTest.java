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
package org.camunda.bpm.spring.boot.starter.webapp;

import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.property.WebappProperty;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class CamundaBpmWebappAutoConfigurationIntegrationTest {

  private String bpmEnabled = CamundaBpmProperties.PREFIX + ".enabled=true";

  private String bpmDisabled = CamundaBpmProperties.PREFIX + ".enabled=false";

  private String webappEnabled = WebappProperty.PREFIX + ".enabled=true";

  private String webappDisabled = WebappProperty.PREFIX + ".enabled=false";

  private WebApplicationContextRunner contextRunner;

  @Before
  public void setUp() {
    AutoConfigurations autoConfigurationsUnderTest = AutoConfigurations.of(CamundaBpmAutoConfiguration.class, CamundaBpmWebappAutoConfiguration.class);
    AutoConfigurations additionalAutoConfigurations = AutoConfigurations.of(DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class);
    contextRunner = new WebApplicationContextRunner().withConfiguration(autoConfigurationsUnderTest).withConfiguration(additionalAutoConfigurations);
  }

  @Test
  public void test_bpmIsNotDisabled_and_webappIsNotDisabled_shouldInitWebapp() {
    contextRunner.run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).hasSingleBean(CamundaBpmWebappInitializer.class);
    });
  }

  @Test
  public void test_bpmIsEnabled_and_webappIsNotDisabled_shouldInitWebapp() {
    contextRunner.withPropertyValues(bpmEnabled).run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).hasSingleBean(CamundaBpmWebappInitializer.class);
    });
  }

  @Test
  public void test_bpmIsDisabled_and_webappIsNotDisabled_shouldNotInitWebapp() {
    contextRunner.withPropertyValues(bpmDisabled).run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).doesNotHaveBean(CamundaBpmWebappInitializer.class);
    });
  }

  @Test
  public void test_bpmIsNotDisabled_and_webappIsEnabled_shouldInitWebapp() {
    contextRunner.withPropertyValues(webappEnabled).run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).hasSingleBean(CamundaBpmWebappInitializer.class);
    });
  }

  @Test
  public void test_bpmIsEnabled_and_webappIsEnabled_shouldInitWebapp() {
    contextRunner.withPropertyValues(bpmEnabled, webappEnabled).run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).hasSingleBean(CamundaBpmWebappInitializer.class);
    });
  }

  @Test
  public void test_bpmIsDisabled_and_webappIsEnabled_shouldNotInitWebapp() {
    contextRunner.withPropertyValues(bpmDisabled, webappEnabled).run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).doesNotHaveBean(CamundaBpmWebappInitializer.class);
    });
  }

  @Test
  public void test_bpmIsNotDisabled_and_webappIsDisabled_shouldNotInitWebapp() {
    contextRunner.withPropertyValues(webappDisabled).run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).doesNotHaveBean(CamundaBpmWebappInitializer.class);
    });
  }

  @Test
  public void test_bpmIsEnabled_and_webappIsDisabled_shouldNotInitWebapp() {
    contextRunner.withPropertyValues(bpmEnabled, webappDisabled).run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).doesNotHaveBean(CamundaBpmWebappInitializer.class);
    });
  }

  @Test
  public void test_bpmIsDisabled_and_webappIsDisabled_shouldNotInitWebapp() {
    contextRunner.withPropertyValues(bpmDisabled, webappDisabled).run(context -> {
      assertThat(context).hasNotFailed();
      assertThat(context).doesNotHaveBean(CamundaBpmWebappInitializer.class);
    });
  }
}
