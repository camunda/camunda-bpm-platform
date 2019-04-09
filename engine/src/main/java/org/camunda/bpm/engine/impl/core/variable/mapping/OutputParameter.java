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

import org.camunda.bpm.engine.impl.core.CoreLogger;
import org.camunda.bpm.engine.impl.core.variable.mapping.value.ParameterValueProvider;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;

/**
 *
 * <pre>
 *   +--------------+
 *   |              |
 *   |  inner scope ------> outer scope
 *   |              |
 *   +--------------+
 * </pre>
 *
 * @author Daniel Meyer
 *
 */
public class OutputParameter extends IoParameter {

  private final static CoreLogger LOG = CoreLogger.CORE_LOGGER;

  public OutputParameter(String name, ParameterValueProvider valueProvider) {
    super(name, valueProvider);
  }

  protected void execute(AbstractVariableScope innerScope, AbstractVariableScope outerScope) {

    // get value from inner scope
    Object value = valueProvider.getValue(innerScope);

    LOG.debugMappingValuefromInnerScopeToOuterScope(value, innerScope, name, outerScope);

    // set variable in outer scope
    outerScope.setVariable(name, value);
  }

}
