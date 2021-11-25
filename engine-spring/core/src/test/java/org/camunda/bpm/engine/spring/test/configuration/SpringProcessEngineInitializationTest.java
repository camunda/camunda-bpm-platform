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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.spring.SpringConfigurationHelper;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class SpringProcessEngineInitializationTest {

  @Test
  public void testSpringConfigurationContextInitializing() {
    //given
    URL resource = existActivitiContext();
    //when
    ProcessEngine processEngine = SpringConfigurationHelper.buildProcessEngine(resource);
    //then
    assertNotNull(processEngine);
    assertEquals("activitiContextProcessName", processEngine.getName());

    processEngine.close();
  }

  @Test
  public void shouldInitializeProcessEngineFromActivitiContext() {
    //given
    existActivitiContext();
    //when
    ProcessEngine processEngine = ProcessEngines.getProcessEngine("activitiContextProcessName");
    //then
    assertNotNull(processEngine);
    assertEquals("activitiContextProcessName", processEngine.getName());

    processEngine.close();
  }

  //check exists activiti-context.xml in classpath without this check ProcessEngines will ignore parsing this file
  private URL existActivitiContext() {
    URL resource = ReflectUtil.getClassLoader().getResource("activiti-context.xml");
    if (resource == null)
      fail("activiti-context.xml not found on the classpath: " + System.getProperty("java.class.path"));
    return resource;
  }
}
