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
package org.camunda.connect.plugin.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.core.variable.scope.SimpleVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.scope.SimpleVariableInstance.SimpleVariableInstanceFactory;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceFactory;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableInstanceLifecycleListener;
import org.camunda.bpm.engine.impl.core.variable.scope.VariableStore;
import org.camunda.connect.spi.ConnectorRequest;
import org.camunda.connect.spi.ConnectorResponse;

/**
 * Exposes a connector request as variableScope.
 *
 * @author Daniel Meyer
 *
 */
public class ConnectorVariableScope extends AbstractVariableScope {

  private static final long serialVersionUID = 1L;

  protected AbstractVariableScope parent;

  protected VariableStore<SimpleVariableInstance> variableStore;

  public ConnectorVariableScope(AbstractVariableScope parent) {
    this.parent = parent;
    this.variableStore = new VariableStore<SimpleVariableInstance>();
  }

  public String getVariableScopeKey() {
    return "connector";
  }

  protected VariableStore<CoreVariableInstance> getVariableStore() {
    return (VariableStore) variableStore;
  }

  @Override
  protected VariableInstanceFactory<CoreVariableInstance> getVariableInstanceFactory() {
    return (VariableInstanceFactory) SimpleVariableInstanceFactory.INSTANCE;
  }

  @Override
  protected List<VariableInstanceLifecycleListener<CoreVariableInstance>> getVariableInstanceLifecycleListeners() {
    return Collections.emptyList();
  }

  public AbstractVariableScope getParentVariableScope() {
    return parent;
  }

  public void writeToRequest(ConnectorRequest<?> request) {
    for (CoreVariableInstance variable : variableStore.getVariables()) {
      request.setRequestParameter(variable.getName(), variable.getTypedValue(true).getValue());
    }
  }

  public void readFromResponse(ConnectorResponse response) {
    Map<String, Object> responseParameters = response.getResponseParameters();
    for (Entry<String, Object> entry : responseParameters.entrySet()) {
      setVariableLocal(entry.getKey(), entry.getValue());
    }
  }

}
