/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.dmn.juel;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class JuelCompiledScript extends CompiledScript {

  protected JuelScriptEngine scriptEngine;
  protected String expression;

  public JuelCompiledScript(JuelScriptEngine scriptEngine, String expression) {
    this.scriptEngine = scriptEngine;
    this.expression = expression;
  }

  public Object eval(ScriptContext context) throws ScriptException {
    return scriptEngine.eval(expression, context);
  }

  public ScriptEngine getEngine() {
    return scriptEngine;
  }

}
