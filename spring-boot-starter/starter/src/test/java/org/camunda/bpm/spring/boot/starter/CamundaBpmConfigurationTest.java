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
package org.camunda.bpm.spring.boot.starter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.junit.Test;

public class CamundaBpmConfigurationTest {

  @Test
  public void processEngineConfigurationImplTest() {
    CamundaBpmConfiguration camundaBpmConfiguration = new CamundaBpmConfiguration();
    List<ProcessEnginePlugin> processEnginePlugins = createUnordedList();
    ProcessEngineConfigurationImpl processEngineConfigurationImpl = camundaBpmConfiguration.processEngineConfigurationImpl(processEnginePlugins);

    CompositeProcessEnginePlugin compositeProcessEnginePlugin = (CompositeProcessEnginePlugin) processEngineConfigurationImpl.getProcessEnginePlugins().get(0);
    assertThat(compositeProcessEnginePlugin.getPlugins()).isEqualTo(processEnginePlugins);
  }

  private List<ProcessEnginePlugin> createUnordedList() {
    List<ProcessEnginePlugin> list = new ArrayList<ProcessEnginePlugin>();
    list.add(mock(ProcessEnginePlugin.class));
    list.add(mock(ProcessEnginePlugin.class));
    return list;
  }

}
