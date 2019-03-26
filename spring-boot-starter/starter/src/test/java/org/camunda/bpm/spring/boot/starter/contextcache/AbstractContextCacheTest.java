/*
 * Copyright © 2015-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.spring.boot.starter.contextcache;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nikola Koevski
 */
public abstract class AbstractContextCacheTest {

  @Autowired
  protected ApplicationContext applicationContext;

  @Autowired
  protected ProcessEngine processEngine;

  @Autowired
  protected RuntimeService runtimeService;

  protected String testName;
  protected static final Map<String, Integer> contextMap = new HashMap<>(3);
  protected String processEngineName;

  @Test
  public void testDbIsolation() {
    // do
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("TestProcess");
    assertThat(instance).isNotNull();

    long numInstances = runtimeService.createProcessInstanceQuery().count();
    assertThat(numInstances).isEqualTo(1);
  }

  @Test
  public void testEngineName()
  {
    assertThat(processEngine.getName()).isEqualTo(processEngineName);
  }

  @Test
  public void testEngineRegistration()
  {
    ProcessEngine registeredEngine = ProcessEngines.getProcessEngine("default");
    assertThat(registeredEngine).isNotSameAs(processEngine);
  }
}
