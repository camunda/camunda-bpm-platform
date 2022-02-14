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
package org.camunda.bpm.engine.impl.pvm.runtime;

import java.util.Map;

import org.camunda.bpm.engine.impl.core.instance.CoreExecution;

/**
 * Keeps track of the execution state when a hierarchy of
 * scopes is instantiated at once (e.g. during process instance modification
 * or process instance migration).
 *
 * State is for example the scopes that need to be instantiated or the
 * variables that need to be set after the scopes are created.
 *
 * @author Sebastian Menski
 */
public class ScopeInstantiationContext {


  protected InstantiationStack instantiationStack;
  protected Map<String, Object> variables;
  protected Map<String, Object> variablesLocal;

  public void applyVariables(CoreExecution execution) {
    execution.setVariables(variables);
    execution.setVariablesLocal(variablesLocal);
  }

  public InstantiationStack getInstantiationStack() {
    return instantiationStack;
  }

  public void setInstantiationStack(InstantiationStack instantiationStack) {
    this.instantiationStack = instantiationStack;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public void setVariablesLocal(Map<String, Object> variablesLocal) {
    this.variablesLocal = variablesLocal;
  }
}
