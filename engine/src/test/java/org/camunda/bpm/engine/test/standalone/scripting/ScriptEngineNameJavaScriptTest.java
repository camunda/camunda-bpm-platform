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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.scripting.engine.DefaultScriptEngineResolver;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ScriptEngineNameJavaScriptTest {

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected String defaultJsSciptEngineName;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    defaultJsSciptEngineName = processEngineConfiguration.getScriptEngineNameJavaScript();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setScriptEngineNameJavaScript(defaultJsSciptEngineName);
  }

  @Test
  public void shouldFindDefaultEngineForJavaScript() {
    // when
    ScriptEngine scriptEngine = getScriptEngine(ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE);

    // then
    assertThat(scriptEngine).isNotNull();
    assertThat(scriptEngine.getFactory().getEngineName()).isEqualTo(ScriptingEngines.GRAAL_JS_SCRIPT_ENGINE_NAME);
  }

  @Test
  public void shouldFindDefaultEngineForEcmaScript() {
    // when
    ScriptEngine scriptEngine = getScriptEngine(ScriptingEngines.ECMASCRIPT_SCRIPTING_LANGUAGE);

    // then
    assertThat(scriptEngine).isNotNull();
    assertThat(scriptEngine.getFactory().getEngineName()).isEqualTo(ScriptingEngines.GRAAL_JS_SCRIPT_ENGINE_NAME);
  }

  @Test
  public void shouldFindDefaultEngineForJavaScriptInPa() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // when
    ScriptEngine scriptEngine = getScriptEngineFromPa(ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE, processApplication);

    // then
    assertThat(scriptEngine).isNotNull();
    assertThat(scriptEngine.getFactory().getEngineName()).isEqualTo(ScriptingEngines.GRAAL_JS_SCRIPT_ENGINE_NAME);
  }

  @Test
  public void shouldFindDefaultEngineForEcmaScriptInPa() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // when
    ScriptEngine scriptEngine = getScriptEngineFromPa(ScriptingEngines.ECMASCRIPT_SCRIPTING_LANGUAGE, processApplication);

    // then
    assertThat(scriptEngine).isNotNull();
    assertThat(scriptEngine.getFactory().getEngineName()).isEqualTo(ScriptingEngines.GRAAL_JS_SCRIPT_ENGINE_NAME);
  }

  @Test
  public void shouldFailIfDefinedEngineCannotBeFound() {
    // given
    processEngineConfiguration.setScriptEngineNameJavaScript("undefined");

    // when
    assertThatThrownBy(() -> getScriptEngine(ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE))
    // then
      .isInstanceOf(NullValueException.class);
  }

  @Test
  public void shouldFailIfDefinedEngineCannotBeFoundInPa() {
    // given
    processEngineConfiguration.setScriptEngineNameJavaScript("undefined");
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();
    // when
    assertThatThrownBy(() -> getScriptEngineFromPa(ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE, processApplication))
    // then
      .isInstanceOf(NullValueException.class);
  }

  @Test
  public void shouldFallbackToAnyEngineForJavaScriptIfDefaultUnavailable() {
    // given
    ScriptEngineManager mockScriptEngineManager = mock(ScriptEngineManager.class);
    ScriptEngine mockScriptEngine = mock(ScriptEngine.class);
    ScriptEngineFactory mockScriptEngineFactory = mock(ScriptEngineFactory.class);
    ScriptingEngines scriptingEngines = new ScriptingEngines(new DefaultScriptEngineResolver(mockScriptEngineManager));

    when(mockScriptEngineManager.getEngineByName(ScriptingEngines.GRAAL_JS_SCRIPT_ENGINE_NAME)).thenReturn(null);
    when(mockScriptEngineManager.getEngineByName(ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE)).thenReturn(mockScriptEngine);
    when(mockScriptEngine.getFactory()).thenReturn(mockScriptEngineFactory);
    when(mockScriptEngineFactory.getEngineName()).thenReturn("foo");

    // when
    ScriptEngine scriptEngine = processEngineConfiguration.getCommandExecutorTxRequired().execute(
        c -> scriptingEngines.getScriptEngineForLanguage(ScriptingEngines.JAVASCRIPT_SCRIPTING_LANGUAGE));

    // then
    assertThat(scriptEngine).isEqualTo(mockScriptEngine);
  }

  protected ScriptingEngines getScriptingEngines() {
    return processEngineConfiguration.getScriptingEngines();
  }

  protected ScriptEngine getScriptEngine(final String name) {
    final ScriptingEngines scriptingEngines = getScriptingEngines();
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(
        c -> scriptingEngines.getScriptEngineForLanguage(name));
  }

  protected ScriptEngine getScriptEngineFromPa(final String name, final ProcessApplicationInterface processApplication) {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(
        c -> Context.executeWithinProcessApplication(() -> getScriptEngine(name), processApplication.getReference()));
  }
}
