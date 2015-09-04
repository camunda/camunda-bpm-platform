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
package org.camunda.bpm.integrationtest.functional.scriptengine.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngineFactory;

/**
 * @author Roman Smirnov
 *
 */
public class DummyScriptEngineFactory implements ScriptEngineFactory {

  public static final String NAME = "dummy";
  public static final String VERSION = "0.1.0";

  public static final List<String> NAMES;
  public static final List<String> EXTENSIONS;
  public static final List<String> MIME_TYPES;

  static {
    NAMES = Collections.unmodifiableList(Arrays.asList(NAME, "Dummy"));
    EXTENSIONS = Collections.emptyList();
    MIME_TYPES = Collections.emptyList();
  }

  public String getEngineName() {
    return NAME;
  }

  public String getEngineVersion() {
    return VERSION;
  }

  public List<String> getExtensions() {
    return EXTENSIONS;
  }

  public List<String> getMimeTypes() {
    return MIME_TYPES;
  }

  public List<String> getNames() {
    return NAMES;
  }

  public String getLanguageName() {
    return NAME;
  }

  public String getLanguageVersion() {
    return VERSION;
  }

  public Object getParameter(String key) {
    if (key.equals("THREADING")) {
      return "MULTITHREADED";
    }
    return null;
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

  public DummyScriptEngine getScriptEngine() {
    return new DummyScriptEngine();
  }

}
