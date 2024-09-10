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
package org.camunda.templateengines;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * {@link ScriptEngineFactory} to create a JSR 223 compatible
 * wrapper of the FreeMarker template engine.
 *
 * @author Sebastian Menski
 */
public class FreeMarkerScriptEngineFactory implements ScriptEngineFactory {

  public final static String NAME = "freemarker";
  public final static String VERSION = "2.3.29";

  public final static List<String> names;
  public final static List<String> extensions;
  public final static List<String> mimeTypes;

  static {
    names = Collections.unmodifiableList(Arrays.asList(NAME, "Freemarker", "FreeMarker"));
    extensions = Collections.unmodifiableList(Collections.singletonList("ftl"));
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
    return "${" + object + "." + method + "(" + joinStrings(", ", args) + ")}";
  }

  public String getOutputStatement(String toDisplay) {
    return toDisplay;
  }

  public String getProgram(String... statements) {
    return joinStrings("\n", statements);
  }

  protected String joinStrings(String delimiter, String[] values) {
    if (values == null) {
      return null;
    }
    else {
      return String.join(delimiter, values);
    }
  }

  public ScriptEngine getScriptEngine() {
    return new FreeMarkerScriptEngine(this);
  }

}
