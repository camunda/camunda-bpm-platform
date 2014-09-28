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
package org.camunda.bpm.engine.impl.scripting;

import javax.script.CompiledScript;

import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;

/**
 * <p>A script factory is responsible for creating a {@link ExecutableScript}
 * instance. Users may customize (subclass) this class in order to customize script
 * compilation. For instance, some users may choose to pre-process scripts before
 * they are compiled.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ScriptFactory {

  protected ScriptingEngines scriptingEngines;
  protected boolean compileScripts;


  public ScriptFactory(ScriptingEngines scriptingEngines, boolean compileScripts) {
    this.scriptingEngines = scriptingEngines;
    this.compileScripts = compileScripts;
  }

  public ExecutableScript createScript(String src, String language) {

    CompiledScript compiledScript = null;
    if (compileScripts) {
      compiledScript = scriptingEngines.compile(src, language);
    }

    if(compiledScript != null) {
      return new CompiledExecutableScript(language, compiledScript);

    } else {
      return new SourceExecutableScript(language, src);

    }
  }

  public ScriptingEngines getScriptingEngines() {
    return scriptingEngines;
  }

}
