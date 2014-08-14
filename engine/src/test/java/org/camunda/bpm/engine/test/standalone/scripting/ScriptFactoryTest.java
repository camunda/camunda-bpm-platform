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

import org.camunda.bpm.engine.impl.scripting.CompiledExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.SourceExecutableScript;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Stefan Hentschel.
 */
public class ScriptFactoryTest extends PluggableProcessEngineTestCase {

  protected static final String SCRIPT_LANGUAGE = "groovy";
  protected static final String EXAMPLE_SCRIPT = "execution.setVariable('aName', 'aValue')";


  public void testDisableScriptCompilation() {
    ScriptFactory factory = new ScriptFactory(processEngineConfiguration.getScriptingEngines(), true);

    ExecutableScript createdScript = factory.createScript(EXAMPLE_SCRIPT, SCRIPT_LANGUAGE);
    assertNotNull(createdScript);
    assertTrue(createdScript instanceof CompiledExecutableScript);

    factory = new ScriptFactory(processEngineConfiguration.getScriptingEngines(), false);

    createdScript = factory.createScript(EXAMPLE_SCRIPT, SCRIPT_LANGUAGE);
    assertNotNull(createdScript);
    assertTrue(createdScript instanceof SourceExecutableScript);
  }

  public void testDefaultScriptCompilationEnabled() {
    ExecutableScript createdScript = processEngineConfiguration.getScriptFactory().createScript(EXAMPLE_SCRIPT, SCRIPT_LANGUAGE);
    assertNotNull(createdScript);
    assertTrue(createdScript instanceof CompiledExecutableScript);
  }

}
