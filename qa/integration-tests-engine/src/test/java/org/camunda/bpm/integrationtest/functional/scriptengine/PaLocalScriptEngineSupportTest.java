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
package org.camunda.bpm.integrationtest.functional.scriptengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.camunda.bpm.integrationtest.functional.scriptengine.engine.AbstractScriptEngineFactory;
import org.camunda.bpm.integrationtest.functional.scriptengine.engine.DummyScriptEngineFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */

public class PaLocalScriptEngineSupportTest extends AbstractPaLocalScriptEngineTest {

  @Deployment
  public static WebArchive createProcessApplication() {
    return initWebArchiveDeployment()
      .addClass(AbstractPaLocalScriptEngineTest.class)
      .addClass(AbstractScriptEngineFactory.class)
      .addClass(DummyScriptEngineFactory.class)
      .addAsResource(new StringAsset(DUMMY_SCRIPT_ENGINE_FACTORY_SPI), SCRIPT_ENGINE_FACTORY_PATH)
      .addAsResource(createScriptTaskProcess(SCRIPT_FORMAT, SCRIPT_TEXT), "process.bpmn20.xml");
  }

  @Test
  public void shouldSetVariable() {
    String processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_ID).getId();
    Object scriptValue = runtimeService.getVariable(processInstanceId, "scriptValue");
    assertNotNull(scriptValue);
    assertEquals(SCRIPT_TEXT, scriptValue);
  }

}
