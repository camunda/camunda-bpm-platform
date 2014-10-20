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

package org.camunda.spin.impl.test;

import java.util.Map;

import org.camunda.spin.SpinScriptException;
import org.junit.ClassRule;
import org.junit.Rule;

/**
 * Base script test which loads an engine and provides the
 * script as field.
 *
 * @author Sebastian Menski
 */
public abstract class ScriptTest {

  @ClassRule
  public static ScriptEngineRule scriptEngine = new ScriptEngineRule();

  @Rule
  public ScriptRule script = new ScriptRule();

  protected void failingWithException() throws Throwable {
    try {
      script.execute();
    }
    catch (SpinScriptException e) {
      throw e.getCause().getCause().getCause();
    }
  }

  protected void failingWithException(Map<String, Object> variables) throws Throwable {
    try {
      script.execute(variables);
    }
    catch (SpinScriptException e) {
      throw e.getCause().getCause().getCause();
    }
  }

}
