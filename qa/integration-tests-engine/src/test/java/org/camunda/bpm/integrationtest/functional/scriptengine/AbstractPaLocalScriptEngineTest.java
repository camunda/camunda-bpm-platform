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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.runner.RunWith;

/**
 * @author Roman Smirnov
 *
 */
@RunWith(Arquillian.class)
public abstract class AbstractPaLocalScriptEngineTest extends AbstractFoxPlatformIntegrationTest {

  public static final String PROCESS_ID = "testProcess";
  public static final String SCRIPT_TEXT = "my-script";
  public static final String SCRIPT_FORMAT = "dummy";
  public static final String DUMMY_SCRIPT_ENGINE_FACTORY_SPI = "org.camunda.bpm.integrationtest.functional.scriptengine.engine.DummyScriptEngineFactory";
  public static final String SCRIPT_ENGINE_FACTORY_PATH = "META-INF/services/javax.script.ScriptEngineFactory";

  protected static StringAsset createScriptTaskProcess(String scriptFormat, String scriptText) {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(PROCESS_ID)
        .camundaHistoryTimeToLive(180)
      .startEvent()
      .scriptTask()
        .scriptFormat(scriptFormat)
        .scriptText(scriptText)
        .camundaResultVariable("scriptValue")
        .userTask()
      .endEvent()
      .done();
    return new StringAsset(Bpmn.convertToString(modelInstance));
  }

  protected ProcessApplicationInterface getProcessApplication() {
    ProcessApplicationReference reference = processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<ProcessApplicationReference>() {
      public ProcessApplicationReference execute(CommandContext commandContext) {
        ProcessDefinitionEntity definition = commandContext
            .getProcessDefinitionManager()
            .findLatestProcessDefinitionByKey(PROCESS_ID);
        String deploymentId = definition.getDeploymentId();
        ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();
        return processApplicationManager.getProcessApplicationForDeployment(deploymentId);
      }
    });

    assertNotNull(reference);

    ProcessApplicationInterface processApplication = null;
    try {
      processApplication = reference.getProcessApplication();
    } catch (ProcessApplicationUnavailableException e) {
      fail("Could not retrieve process application");
    }

    return processApplication.getRawObject();
  }

}
