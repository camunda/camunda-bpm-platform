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
package org.camunda.bpm.engine.impl.core.variable.mapping;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;

/**
 * An {@link IoParameter} creates a variable
 * in a target variable scope.
 *
 * @author Daniel Meyer
 */
public abstract class IoParameter {

  /**
   * The name of the parameter. The name of the parameter is the
   * variable name in the target {@link VariableScope}.
   */
  protected String name;

  /**
   * The provider of the parameter value.
   */
  protected ParameterValueProvider valueProvider;

  public IoParameter(String name, ParameterValueProvider valueProvider) {
    this.name = name;
    this.valueProvider = valueProvider;
  }

  /**
   * Execute the parameter in a given variable scope.
   */
  public void execute(AbstractVariableScope scope) {
    execute(scope, scope.getParentVariableScope());
  }

   /**
   * @param innerScope
   * @param outerScope
   */
  protected abstract void execute(AbstractVariableScope innerScope, AbstractVariableScope outerScope);

  // getters / setters ///////////////////////////

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ParameterValueProvider getValueProvider() {
    return valueProvider;
  }

  public void setValueProvider(ParameterValueProvider valueProvider) {
    this.valueProvider = valueProvider;
  }

}
