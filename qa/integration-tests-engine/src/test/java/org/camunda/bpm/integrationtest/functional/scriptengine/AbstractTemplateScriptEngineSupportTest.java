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

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Sebastian Menski
 */
@RunWith(Arquillian.class)
public abstract class AbstractTemplateScriptEngineSupportTest extends AbstractFoxPlatformIntegrationTest {

  public static final String PROCESS_ID = "testProcess";
  public static final String RESULT_VARIABLE = "foo";
  public static final String EXAMPLE_TEMPLATE = "Hello ${name}!";
  public static final String EXPECTED_RESULT = "Hello world!";

  public String processInstanceId;

  protected static StringAsset createScriptTaskProcess(String scriptFormat, String scriptText) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .camundaHistoryTimeToLive(180)
      .startEvent()
      .scriptTask()
        .scriptFormat(scriptFormat)
        .scriptText(scriptText)
        .camundaResultVariable(RESULT_VARIABLE)
      .userTask()
      .endEvent()
      .done();
    return new StringAsset(Bpmn.convertToString(modelInstance));
  }

  @Test
  public void shouldEvaluateTemplate() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("name", "world");
    processInstanceId = runtimeService.startProcessInstanceByKey(PROCESS_ID, variables).getId();

    Object result = runtimeService.getVariable(processInstanceId, RESULT_VARIABLE);
    assertNotNull(result);
    assertEquals(EXPECTED_RESULT, result);
  }

}
