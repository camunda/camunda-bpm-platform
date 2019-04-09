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
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableScope;

/**
 * A script which is dynamically determined during the execution.
 * Therefore it has to be executed in the context of an atomic operation.
 *
 * @author Sebastian Menski
 */
public abstract class DynamicExecutableScript extends ExecutableScript {

  protected final Expression scriptExpression;

  protected DynamicExecutableScript(Expression scriptExpression, String language) {
    super(language);
    this.scriptExpression = scriptExpression;
  }

  public Object evaluate(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings) {
    String source = getScriptSource(variableScope);
    try {
      return scriptEngine.eval(source, bindings);
    }
    catch (ScriptException e) {
      String activityIdMessage = getActivityIdExceptionMessage(variableScope);
      throw new ScriptEvaluationException("Unable to evaluate script" + activityIdMessage + ": " + e.getMessage(), e);
    }
  }

  protected String evaluateExpression(VariableScope variableScope) {
    return (String) scriptExpression.getValue(variableScope);
  }

  public abstract String getScriptSource(VariableScope variableScope);

}
