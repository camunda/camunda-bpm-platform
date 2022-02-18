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
package org.camunda.bpm.engine.rest.spi.impl;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.variable.ValueTypeResolverImpl;
import org.camunda.bpm.engine.rest.helper.MockProvider;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.engine.variable.type.ValueTypeResolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockedProcessEngineProvider implements ProcessEngineProvider {

  private static ProcessEngine cachedDefaultProcessEngine;
  private static Map<String, ProcessEngine> cachedEngines = new HashMap<String, ProcessEngine>();

  public void resetEngines() {
    cachedDefaultProcessEngine = null;
    cachedEngines = new HashMap<String, ProcessEngine>();
  }

  private ProcessEngine mockProcessEngine(String engineName) {
    ProcessEngine engine = mock(ProcessEngine.class);
    when(engine.getName()).thenReturn(engineName);
    mockServices(engine);
    return engine;
  }

  private void mockServices(ProcessEngine engine) {
    IdentityService identityService = mock(IdentityService.class);
    ExternalTaskService externalTaskService = mock(ExternalTaskService.class);

    when(engine.getIdentityService()).thenReturn(identityService);
    when(engine.getExternalTaskService()).thenReturn(externalTaskService);
  }

  @Override
  public ProcessEngine getDefaultProcessEngine() {
    if (cachedDefaultProcessEngine == null) {
      cachedDefaultProcessEngine = mockProcessEngine("default");
    }

    return cachedDefaultProcessEngine;
  }

  @Override
  public ProcessEngine getProcessEngine(String name) {
    if (name.equals(MockProvider.NON_EXISTING_PROCESS_ENGINE_NAME)) {
      return null;
    }

    if (name.equals("default")) {
      return getDefaultProcessEngine();
    }

    if (cachedEngines.get(name) == null) {
      ProcessEngine mock = mockProcessEngine(name);
      cachedEngines.put(name, mock);
    }

    return cachedEngines.get(name);
  }

  @Override
  public Set<String> getProcessEngineNames() {
    Set<String> result = new HashSet<String>();
    result.add(MockProvider.EXAMPLE_PROCESS_ENGINE_NAME);
    result.add(MockProvider.ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME);
    return result;
  }


}
