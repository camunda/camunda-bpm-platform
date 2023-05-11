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
package org.camunda.bpm.engine.spring.test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.junit.After;
import org.junit.Test;

public class SpringProcessEngineInitializationTest {
  
  @After
  public void tearDown() {
    ProcessEngines.destroy();
  }
  
  @Test
  public void shouldInitializeProcessEngineFromActivitiContext() {
    // given
    existActivitiContext();
    // when
    ProcessEngine processEngine = ProcessEngines.getProcessEngine("activitiContextProcessName");
    // then
    assertThat(processEngine).isNotNull();
    assertThat(processEngine.getName()).isEqualTo("activitiContextProcessName");
  }

  private URL existActivitiContext() {
    URL resource = ReflectUtil.getClassLoader().getResource("activiti-context.xml");
    assertThat(resource)
      .withFailMessage("activiti-context.xml not found on the classpath: " + System.getProperty("java.class.path"))
      .isNotNull();
    return resource;
  }
  
}
