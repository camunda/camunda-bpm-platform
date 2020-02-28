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
package org.camunda.bpm.engine.test.standalone.initialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineInfo;
import org.camunda.bpm.engine.ProcessEngines;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tom Baeyens
 */
public class ProcessEnginesTest {

  @Before
  public void setUp() throws Exception {
    ProcessEngines.destroy();
    ProcessEngines.init();
  }

  @After
  public void tearDown() throws Exception {
    ProcessEngines.destroy();
  }

  @Test
  public void testProcessEngineInfo() {
    // given
    ProcessEngines.init();

    // when
    List<ProcessEngineInfo> processEngineInfos = ProcessEngines.getProcessEngineInfos();

    // then
    assertEquals(1, processEngineInfos.size());

    ProcessEngineInfo processEngineInfo = processEngineInfos.get(0);
    assertNull(processEngineInfo.getException());
    assertNotNull(processEngineInfo.getName());
    assertNotNull(processEngineInfo.getResourceUrl());

    ProcessEngine processEngine = ProcessEngines.getProcessEngine(ProcessEngines.NAME_DEFAULT);
    assertNotNull(processEngine);
  }

}
