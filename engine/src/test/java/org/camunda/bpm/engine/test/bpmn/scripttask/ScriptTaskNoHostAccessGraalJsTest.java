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
package org.camunda.bpm.engine.test.bpmn.scripttask;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptBindingsFactory;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptEngineResolver;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;

/**
 * Tests GraalVM JavaScript with host access disabled.
 * Host access needs to be consistent for a GraalVM JavaScript instance.
 * Therefore this test class provides its own instance.
 */
public class ScriptTaskNoHostAccessGraalJsTest extends AbstractScriptTaskTest {

  private static final String GRAALJS = "graal.js";

  protected ScriptingEnvironment defaultScriptingEnvironment;

  @Before
  public void setup() {
    defaultScriptingEnvironment = processEngineConfiguration.getScriptingEnvironment();
    processEngineConfiguration.setConfigureScriptEngineHostAccess(false);
    // create custom script engine lookup to receive a fresh GraalVM JavaScript engine
    ScriptingEngines customScriptingEngines = new TestScriptingEngines(new ScriptBindingsFactory(processEngineConfiguration.getResolverFactories()));
    customScriptingEngines.setEnableScriptEngineCaching(processEngineConfiguration.isEnableScriptEngineCaching());
    processEngineConfiguration.setScriptingEnvironment(new ScriptingEnvironment(processEngineConfiguration.getScriptFactory(),
        processEngineConfiguration.getEnvScriptResolvers(), customScriptingEngines));
  }

  @After
  public void resetConfiguration() {
    processEngineConfiguration.setConfigureScriptEngineHostAccess(true);
    processEngineConfiguration.setScriptingEnvironment(defaultScriptingEnvironment);
  }

  @Test
  public void shouldFailOnHostAccessIfNotEnabled() {
    // GIVEN
    // accessing a Java class from JavaScript
    deployProcess(GRAALJS, "execution.setVariable('date', new java.util.Date(0));");

    // WHEN
    // we start an instance of this process
    assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("testProcess"))
    // THEN
    // this is not allowed in the JS ScriptEngine
      .isInstanceOf(ScriptEvaluationException.class)
      .hasMessageContaining("ReferenceError");
  }

  public static class TestScriptingEngines extends ScriptingEngines {

    public TestScriptingEngines(ScriptBindingsFactory scriptBindingsFactory) {
      this(new ScriptEngineManager());
      this.scriptBindingsFactory = scriptBindingsFactory;
    }

    public TestScriptingEngines(ScriptEngineManager scriptEngineManager) {
      super(scriptEngineManager);
      this.scriptEngineResolver = new TestScriptEngineResolver(scriptEngineManager);
    }

  }

  public static class TestScriptEngineResolver extends ScriptEngineResolver {

    public TestScriptEngineResolver(ScriptEngineManager scriptEngineManager) {
      super(scriptEngineManager);
    }

    @Override
    protected ScriptEngine getScriptEngine(String language) {
      if (GRAALJS.equalsIgnoreCase(language)) {
        return new GraalJSEngineFactory().getScriptEngine();
      }
      return super.getScriptEngine(language);
    }
  }
}
