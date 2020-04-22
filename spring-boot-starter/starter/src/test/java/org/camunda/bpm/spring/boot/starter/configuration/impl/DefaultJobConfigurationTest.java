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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Arrays;

import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.NotifyAcquisitionRejectedJobsHandler;
import org.camunda.bpm.engine.impl.jobexecutor.RejectedJobsHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.test.nonpa.TestApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
  classes = {TestApplication.class},
  webEnvironment = WebEnvironment.NONE
)
public class DefaultJobConfigurationTest {

  private final SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
  private final DefaultJobConfiguration jobConfiguration = new DefaultJobConfiguration();
  private final CamundaBpmProperties properties = new CamundaBpmProperties();

  @Autowired
  JobExecutor jobExecutor;

  @Before
  public void setUp() {
    setField(jobConfiguration, "camundaBpmProperties", properties);
  }

  @Test
  public void delegate_to_specialized_configurations() {
    DefaultJobConfiguration configurationSpy = Mockito.spy(jobConfiguration);
    configurationSpy.preInit(processEngineConfiguration);
    verify(configurationSpy).configureJobExecutor(processEngineConfiguration);
    verify(configurationSpy).registerCustomJobHandlers(processEngineConfiguration);
  }

  @Test
  public void addJobHandler() {
    JobHandler<?> jobHandler = mock(JobHandler.class);
    when(jobHandler.getType()).thenReturn("MockHandler");
    setField(jobConfiguration, "customJobHandlers", Arrays.<JobHandler<?>> asList(jobHandler));

    assertThat(processEngineConfiguration.getCustomJobHandlers()).isNull();
    jobConfiguration.registerCustomJobHandlers(processEngineConfiguration);

    assertThat(processEngineConfiguration.getCustomJobHandlers()).containsOnly(jobHandler);
  }

  @Test
  public void shouldUseDefaultRejectedJobsHandler() {
    // given default configuration

    // when
    RejectedJobsHandler rejectedJobsHandler = jobExecutor.getRejectedJobsHandler();

    // then
    assertThat(rejectedJobsHandler).isInstanceOf(NotifyAcquisitionRejectedJobsHandler.class);
  }

}
