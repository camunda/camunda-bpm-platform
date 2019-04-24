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
package org.camunda.bpm.integrationtest.functional.spin;

import static org.camunda.bpm.engine.variable.Variables.serializedObjectValue;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.Spin.XML;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.spin.impl.util.SpinIoUtil;
import org.camunda.spin.json.SpinJsonNode;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>Smoketest Make sure camunda spin can be used in a process application </p>
 *
 * @author Daniel Meyer
 */
@RunWith(Arquillian.class)
public class PaSpinSupportTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment()
        .addAsResource("org/camunda/bpm/integrationtest/oneTaskProcess.bpmn")
        .addAsResource("org/camunda/bpm/integrationtest/functional/spin/jackson146.json");
  }

  @Test
  public void spinShouldBeAvailable() {
    Assert.assertEquals("someXml", XML("<someXml />").xPath("/someXml").element().name());
  }

  @Test
  public void spinCanBeUsedForVariableSerialization() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", Variables.createVariables()
        .putValue("serializedObject", serializedObjectValue("{\"foo\": \"bar\"}").serializationDataFormat("application/json").objectTypeName(HashMap.class.getName())));

    ObjectValue objectValue = runtimeService.getVariableTyped(pi.getId(), "serializedObject", true);

    HashMap<String, String> expected = new HashMap<String, String>();
    expected.put("foo", "bar");

    Assert.assertEquals(expected, objectValue.getValue());
  }

  @Test
  public void spinPluginShouldBeRegistered() {

    List<ProcessEnginePlugin> processEnginePlugins = processEngineConfiguration.getProcessEnginePlugins();

    boolean spinPluginFound = false;

    for (ProcessEnginePlugin plugin : processEnginePlugins) {
      if (plugin.getClass().getName().contains("Spin")) {
        spinPluginFound = true;
        break;
      }
    }

    Assert.assertTrue(spinPluginFound);
  }

  @Test
  public void testJacksonBug146() {
    InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/integrationtest/functional/spin/jackson146.json");
    String jackson146 = SpinIoUtil.inputStreamAsString(resourceAsStream);

    // this should not fail
    SpinJsonNode node = JSON(jackson146);

    // file has 4000 characters in length a
    // 20 characters per repeated JSON object
    assertEquals(200, node.prop("abcdef").elements().size());
  }

  @Test
  public void testJacksonBug146AsVariable() {
    InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("org/camunda/bpm/integrationtest/functional/spin/jackson146.json");
    String jackson146 = SpinIoUtil.inputStreamAsString(resourceAsStream);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", Variables.createVariables()
      .putValue("jackson146", serializedObjectValue(jackson146).serializationDataFormat("application/json").objectTypeName(HashMap.class.getName())));

    // file has 4000 characters in length a
    // 20 characters per repeated JSON object
    ObjectValue objectValue = runtimeService.getVariableTyped(pi.getId(), "jackson146", true);
    HashMap<String, List<Object>> map = (HashMap<String, List<Object>>) objectValue.getValue();

    assertEquals(200, map.get("abcdef").size());
  }

}
