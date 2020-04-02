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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class SpinJsonPathTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive createDeployment() {
    return initWebArchiveDeployment()
        .addAsResource("org/camunda/bpm/integrationtest/jsonpath-process.bpmn");
  }

  @Test
  public void shouldEvaluateJsonPath() {
    // given
    String json = "{ " +
        "   \"foo\":[ " +
        "      { " +
        "         \"bazz\": \"hello\"," +
        "         \"bar\":[ " +
        "            42" +
        "         ]" +
        "      }," +
        "      { " +
        "         \"bazz\": \"world\"," +
        "         \"bar\":[ " +
        "            42" +
        "         ]" +
        "      }" +
        "   ]" +
        "}";

    try {
      // when
      runtimeService.startProcessInstanceByKey("jsonpath-process",
          Variables.createVariables().putValue("data", json));

    } catch (ProcessEngineException e) {
      fail(e.getMessage());

    } // then: no exception thrown
  }

}
