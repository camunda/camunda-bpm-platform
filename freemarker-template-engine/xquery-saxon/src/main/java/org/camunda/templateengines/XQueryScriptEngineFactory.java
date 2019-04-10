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

import org.camunda.commons.utils.StringUtil;

/**
 * 
 * {@link ScriptEngineFactory} to create a JSR 223 compatible wrapper of the Saxon XQuery processor.
 * 
 */
public class XQueryScriptEngineFactory implements ScriptEngineFactory {

  public final static String NAME = "xquery";
  public final static String ENGINE_NAME = "saxon";
  public final static String ENGINE_VERSION = "9.6";
  public final static String XQUERY_VERSION = "2.0";

  public final static List<String> names;
  public final static List<String> extensions;
  public final static List<String> mimeTypes;

  static {
    names = Collections.unmodifiableList(Arrays.asList(NAME, "XQuery", "xquery"));
    extensions = Collections.unmodifiableList(Arrays.asList("xquery", "xq"));
    mimeTypes = Collections.emptyList();
  }

  public String getEngineName() {
    return ENGINE_NAME;
  }

  public String getEngineVersion() {
    return ENGINE_VERSION;
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
    return XQUERY_VERSION;
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
    // as close as we will get
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      if (i > 0) {
        stringBuilder.append(", ");
      }
      stringBuilder.append('$');
      stringBuilder.append(args[i]);
    }

    return "{" + object + ":" + method + "(" + stringBuilder + ")}";
  }

  public String getOutputStatement(String toDisplay) {
    return toDisplay;
  }

  public String getProgram(String... statements) {
    return StringUtil.join("\n", statements);
  }

  public ScriptEngine getScriptEngine() {
    return new XQueryScriptEngine(this);
  }

}
