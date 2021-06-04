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
package org.camunda.bpm.engine.test.standalone.scripting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.env.ScriptEnvResolver;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.junit.Test;

public class EnvScriptResolutionTest extends AbstractScriptEnvironmentTest {

  protected static final String SCRIPT_LANGUAGE = "graal.js";
  protected static final String ECMASCRIPT_LANGUAGE = "ecmascript";
  protected static final String SCRIPT = "print('hello world');";
  protected static final String ENV_SCRIPT = "print('hello world from env script');";

  @Override
  protected ScriptEnvResolver getResolver() {
    return language -> ECMASCRIPT_LANGUAGE.equals(language) ? new String[] { ENV_SCRIPT } : null;
  }

  @Override
  protected String getScript() {
    return SCRIPT;
  }

  @Test
  public void shouldFindEnvScriptForLanguage() {
    // given
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource(processPath)
        .deploy();

    // when
    executeScript(processApplication, ECMASCRIPT_LANGUAGE);

    // then
    Map<String, List<ExecutableScript>> environmentScripts = processApplication.getEnvironmentScripts();
    assertThat(environmentScripts)
      .hasSize(1)
      .containsKey(ECMASCRIPT_LANGUAGE)
      .extracting(ECMASCRIPT_LANGUAGE)
        .hasSize(1);

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Test
  public void shouldFindEnvScriptForScriptEngineLanguageIfLanguageNotFound() {
    // given
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource(processPath)
        .deploy();

    // when
    executeScript(processApplication, SCRIPT_LANGUAGE);

    // then
    Map<String, List<ExecutableScript>> environmentScripts = processApplication.getEnvironmentScripts();
    assertThat(environmentScripts)
      .hasSize(2)
      .containsKeys(ECMASCRIPT_LANGUAGE, SCRIPT_LANGUAGE)
      .containsEntry(SCRIPT_LANGUAGE, Collections.emptyList())
      .extracting(ECMASCRIPT_LANGUAGE)
        .hasSize(1);

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

}
