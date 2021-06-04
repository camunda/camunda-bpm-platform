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
package org.camunda.bpm.engine.impl.scripting.engine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

public interface ScriptEngineResolver {

  void addScriptEngineFactory(ScriptEngineFactory scriptEngineFactory);

  ScriptEngineManager getScriptEngineManager();

  /**
   * Returns a cached script engine or creates a new script engine if no such engine is currently cached.
   *
   * @param language the language (such as 'groovy' for the script engine)
   * @return the cached engine or null if no script engine can be created for the given language
   */
  ScriptEngine getScriptEngine(String language, boolean resolveFromCache);
}
