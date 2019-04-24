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
package org.camunda.bpm.engine.impl.scripting;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

public class CompiledExecutableScript extends ExecutableScript {

  private final static ScriptLogger LOG = ProcessEngineLogger.SCRIPT_LOGGER;

  protected CompiledScript compiledScript;

  protected CompiledExecutableScript(String language) {
    this(language, null);
  }

  protected CompiledExecutableScript(String language, CompiledScript compiledScript) {
    super(language);
    this.compiledScript = compiledScript;
  }

  public CompiledScript getCompiledScript() {
    return compiledScript;
  }

  public void setCompiledScript(CompiledScript compiledScript) {
    this.compiledScript = compiledScript;
  }

  public Object evaluate(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings) {
    try {
      LOG.debugEvaluatingCompiledScript(language);
      return getCompiledScript().eval(bindings);
    } catch (ScriptException e) {
      if (e.getCause() instanceof BpmnError) {
        throw (BpmnError) e.getCause();
      }
      String activityIdMessage = getActivityIdExceptionMessage(variableScope);
      throw new ScriptEvaluationException("Unable to evaluate script" + activityIdMessage +": " + e.getMessage(), e);
    }
  }

}
