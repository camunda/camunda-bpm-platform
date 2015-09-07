/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.standalone.scripting;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.SourceExecutableScript;
import org.camunda.bpm.engine.impl.scripting.env.ScriptingEnvironment;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Stefan Hentschel.
 */
public class ScriptCompilationTest extends PluggableProcessEngineTestCase {

  protected static final String SCRIPT_LANGUAGE = "groovy";
  protected static final String EXAMPLE_SCRIPT = "println 'hello world'";

  protected ScriptFactory scriptFactory;

  public void setUp() {
    scriptFactory = processEngineConfiguration.getScriptFactory();
  }

  protected SourceExecutableScript createScript(String language, String source) {
    return (SourceExecutableScript) scriptFactory.createScriptFromSource(language, source);
  }

  public void testScriptShouldBeCompiledByDefault() {
    // when a script is created
    SourceExecutableScript script = createScript(SCRIPT_LANGUAGE, EXAMPLE_SCRIPT);
    assertNotNull(script);

    // then it should not be compiled on creation
    assertTrue(script.isShouldBeCompiled());
    assertNull(script.getCompiledScript());

    // but after first execution
    executeScript(script);

    // it was compiled
    assertFalse(script.isShouldBeCompiled());
    assertNotNull(script.getCompiledScript());
  }

  public void testDisableScriptCompilation() {
    // when script compilation is disabled and a script is created
    processEngineConfiguration.setEnableScriptCompilation(false);
    SourceExecutableScript script = createScript(SCRIPT_LANGUAGE, EXAMPLE_SCRIPT);
    assertNotNull(script);

    // then it should not be compiled on creation
    assertTrue(script.isShouldBeCompiled());
    assertNull(script.getCompiledScript());

    // and after first execution
    executeScript(script);

    // it was also not compiled
    assertFalse(script.isShouldBeCompiled());
    assertNull(script.getCompiledScript());

    // re-enable script compilation
    processEngineConfiguration.setEnableScriptCompilation(true);
  }

  public void testDisableScriptCompilationByDisabledScriptEngineCaching() {
    // when script engine caching is disabled and a script is created
    processEngineConfiguration.setEnableScriptEngineCaching(false);
    SourceExecutableScript script = createScript(SCRIPT_LANGUAGE, EXAMPLE_SCRIPT);
    assertNotNull(script);

    // then it should not be compiled on creation
    assertTrue(script.isShouldBeCompiled());
    assertNull(script.getCompiledScript());

    // and after first execution
    executeScript(script);

    // it was also not compiled
    assertFalse(script.isShouldBeCompiled());
    assertNull(script.getCompiledScript());

    // re-enable script engine caching
    processEngineConfiguration.setEnableScriptEngineCaching(true);
  }

  public void testOverrideScriptSource() {
    // when a script is created and executed
    SourceExecutableScript script = createScript(SCRIPT_LANGUAGE, EXAMPLE_SCRIPT);
    assertNotNull(script);
    executeScript(script);

    // it was compiled
    assertFalse(script.isShouldBeCompiled());
    assertNotNull(script.getCompiledScript());

    // if the script source changes
    script.setScriptSource(EXAMPLE_SCRIPT);

    // then it should not be compiled after change
    assertTrue(script.isShouldBeCompiled());
    assertNull(script.getCompiledScript());

    // but after next execution
    executeScript(script);

    // it is compiled again
    assertFalse(script.isShouldBeCompiled());
    assertNotNull(script.getCompiledScript());
  }

  protected Object executeScript(final ExecutableScript script) {
    final ScriptingEnvironment scriptingEnvironment = processEngineConfiguration.getScriptingEnvironment();
    return processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          return scriptingEnvironment.execute(script, null);
        }
      });
  }

}
