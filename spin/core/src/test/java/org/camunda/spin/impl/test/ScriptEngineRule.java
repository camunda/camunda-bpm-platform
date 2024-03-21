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
package org.camunda.spin.impl.test;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A jUnit4 {@link ClassRule} to define create a {@link ScriptEngine}
 * base on {@literal @}{@link ScriptEngineRule} annotation.
 *
 * @author Sebastian Menski
 */
public class ScriptEngineRule implements TestRule {

  private static final SpinTestLogger LOG = SpinTestLogger.TEST_LOGGER;

  private static final Map<String, ScriptEngine> cachedEngines = new HashMap<>();

  private static final String GRAAL_JS_SCRIPT_ENGINE_NAME = "Graal.js";

  private javax.script.ScriptEngine scriptEngine;


  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      public void evaluate() throws Throwable {
        scriptEngine = createScriptEngine(description);
        if (scriptEngine != null) {
          LOG.scriptEngineFoundForLanguage(scriptEngine.getFactory().getLanguageName());
        }
        base.evaluate();
      }
    };
  }

  /**
   * Create script engine from {@literal @}{@link org.camunda.spin.impl.test.ScriptEngine} Annotation. The created
   * script engines will be cached to speedup subsequent creations.
   *
   * @param description the {@link Description} of the test method
   * @return the script engine or null if no suitable found
   */
  private ScriptEngine createScriptEngine(Description description) {
    org.camunda.spin.impl.test.ScriptEngine annotation = description.getTestClass().getAnnotation(org.camunda.spin.impl.test.ScriptEngine.class);
    if (annotation == null) {
      return null;
    }
    else {
      String language = annotation.value();
      if (!cachedEngines.containsKey(language)) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(language);
        if (engine == null) {
          throw LOG.noScriptEngineFoundForLanguage(language);
        }
        if (GRAAL_JS_SCRIPT_ENGINE_NAME.equals(engine.getFactory().getEngineName())) {
          configureGraalJsScriptEngine(engine);
        }
        cachedEngines.put(language, engine);
      }
      return cachedEngines.get(language);
    }
  }

  /**
   * Get the script engine defined by the {@literal @}{@link org.camunda.spin.impl.test.ScriptEngine} Annotation
   *
   * @return the script engine or null if no script engine was found
   */
  public ScriptEngine getScriptEngine() {
    return scriptEngine;
  }

  protected void configureGraalJsScriptEngine(ScriptEngine scriptEngine) {
    // make sure GraalVM JS can provide access the host and can lookup classes
    scriptEngine.getContext().setAttribute("polyglot.js.allowHostAccess", true, ScriptContext.ENGINE_SCOPE);
    scriptEngine.getContext().setAttribute("polyglot.js.allowHostClassLookup", true, ScriptContext.ENGINE_SCOPE);
  }
}
