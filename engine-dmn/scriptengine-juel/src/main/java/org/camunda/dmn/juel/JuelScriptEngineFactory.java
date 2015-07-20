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

package org.camunda.dmn.juel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.camunda.commons.utils.StringUtil;

public class JuelScriptEngineFactory implements ScriptEngineFactory {

  public static final String NAME = "juel";
  public static final String VERSION = "2.2.7";

  public static final List<String> names;
  public static final List<String> extensions;
  public static final List<String> mimeTypes;

  static {
    names = Collections.unmodifiableList(Arrays.asList(NAME, "Juel", "JUEL"));
    extensions = Collections.unmodifiableList(Collections.singletonList(NAME));
    mimeTypes = Collections.emptyList();
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

  public String getMethodCallSyntax(String object, String method, String... args) {
    return "${" + object + "." + method + "(" + StringUtil.join(", ", args) + ")}";
  }

  public String getOutputStatement(String toDisplay) {
    toDisplay = toDisplay.replace("\"", "\\\"").replace("\\", "\\\\");
    return "out:print(\"" + toDisplay + "\")";
  }

  public String getProgram(String... statements) {
    StringBuilder builder = new StringBuilder();
    for (String statement : statements) {
      builder.append("${").append(statement).append("} ");
    }
    return builder.toString().trim();
  }

  public ScriptEngine getScriptEngine() {
    return new JuelScriptEngine(this);
  }

}
