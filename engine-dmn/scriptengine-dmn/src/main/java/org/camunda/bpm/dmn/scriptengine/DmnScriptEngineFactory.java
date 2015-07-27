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

package org.camunda.bpm.dmn.scriptengine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.camunda.bpm.dmn.engine.DmnEngine;

public class DmnScriptEngineFactory implements ScriptEngineFactory {

  public static final String NAME = "dmn";
  public static final String VERSION = "1.0";

  public static final List<String> names;
  public static final List<String> extensions;
  public static final List<String> mimeTypes;

  static {
    names = Collections.unmodifiableList(Arrays.asList(NAME, "Dmn", "DMN"));
    extensions = Collections.unmodifiableList(Arrays.asList(NAME, "dmn10.xml"));
    mimeTypes = Collections.emptyList();
  }

  protected DmnEngine dmnEngine;

  public DmnScriptEngineFactory() {

  }

  public DmnScriptEngineFactory(DmnEngine dmnEngine) {
    this.dmnEngine = dmnEngine;
  }

  public String getEngineName() {
    return NAME;
  }

  public String getEngineVersion() {
    return VERSION;
  }

  public List<String> getExtensions() {
    return extensions;
  }

  public List<String> getMimeTypes() {
    return mimeTypes;
  }

  public List<String> getNames() {
    return names;
  }

  public String getLanguageName() {
    return NAME;
  }

  public String getLanguageVersion() {
    return VERSION;
  }

  public Object getParameter(String key) {
    if (key.equals(ScriptEngine.NAME)) {
      return getLanguageName();
    } else if (key.equals(ScriptEngine.ENGINE)) {
      return getEngineName();
    } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
      return getEngineVersion();
    } else if (key.equals(ScriptEngine.LANGUAGE)) {
      return getLanguageName();
    } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
      return getLanguageVersion();
    } else if (key.equals("THREADING")) {
      return "MULTITHREADED";
    } else {
      return null;
    }
  }

  public String getMethodCallSyntax(String obj, String m, String... args) {
    throw new UnsupportedOperationException("getMethodCallSyntax");
  }

  public String getOutputStatement(String toDisplay) {
    throw new UnsupportedOperationException("getOutputStatement");
  }

  public String getProgram(String... statements) {
    throw new UnsupportedOperationException("getProgram");
  }

  public ScriptEngine getScriptEngine() {
    if (dmnEngine != null) {
      return new DmnScriptEngine(this, dmnEngine);
    }
    else {
      return new DmnScriptEngine();
    }
  }

}
