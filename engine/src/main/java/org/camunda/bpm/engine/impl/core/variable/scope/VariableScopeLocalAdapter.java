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
package org.camunda.bpm.engine.impl.core.variable.scope;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Wraps a variable scope as if it has no parent such that it is reduced to its local
 * variables. For example {@link #getVariable(String)} simply delegates to
 * {@link #getVariableLocal(String)}.
 *
 * @author Thorben Lindhauer
 */
public class VariableScopeLocalAdapter implements VariableScope {

  protected VariableScope wrappedScope;

  public VariableScopeLocalAdapter(VariableScope wrappedScope) {
    this.wrappedScope = wrappedScope;
  }

  public String getVariableScopeKey() {
    return wrappedScope.getVariableScopeKey();
  }

  public Map<String, Object> getVariables() {
    return getVariablesLocal();
  }

  public VariableMap getVariablesTyped() {
    return getVariablesLocalTyped();
  }

  public VariableMap getVariablesTyped(boolean deserializeValues) {
    return getVariablesLocalTyped(deserializeValues);
  }

  public Map<String, Object> getVariablesLocal() {
    return wrappedScope.getVariablesLocal();
  }

  public VariableMap getVariablesLocalTyped() {
    return wrappedScope.getVariablesLocalTyped();
  }

  public VariableMap getVariablesLocalTyped(boolean deserializeValues) {
    return wrappedScope.getVariablesLocalTyped(deserializeValues);
  }

  public Object getVariable(String variableName) {
    return getVariableLocal(variableName);
  }

  public Object getVariableLocal(String variableName) {
    return wrappedScope.getVariableLocal(variableName);
  }

  public <T extends TypedValue> T getVariableTyped(String variableName) {
    return getVariableLocalTyped(variableName);
  }

  public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeValue) {
    return getVariableLocalTyped(variableName, deserializeValue);
  }

  public <T extends TypedValue> T getVariableLocalTyped(String variableName) {
    return wrappedScope.getVariableLocalTyped(variableName);
  }

  public <T extends TypedValue> T getVariableLocalTyped(String variableName, boolean deserializeValue) {
    return wrappedScope.getVariableLocalTyped(variableName, deserializeValue);
  }

  public Set<String> getVariableNames() {
    return getVariableNamesLocal();
  }

  public Set<String> getVariableNamesLocal() {
    return wrappedScope.getVariableNamesLocal();
  }

  public void setVariable(String variableName, Object value) {
    setVariableLocal(variableName, value);
  }

  public void setVariableLocal(String variableName, Object value) {
    wrappedScope.setVariableLocal(variableName, value);

  }

  public void setVariables(Map<String, ? extends Object> variables) {
    setVariablesLocal(variables);

  }

  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    wrappedScope.setVariablesLocal(variables);

  }

  public boolean hasVariables() {
    return hasVariablesLocal();
  }

  public boolean hasVariablesLocal() {
    return wrappedScope.hasVariablesLocal();
  }

  public boolean hasVariable(String variableName) {
    return hasVariableLocal(variableName);
  }

  public boolean hasVariableLocal(String variableName) {
    return wrappedScope.hasVariableLocal(variableName);
  }

  public void removeVariable(String variableName) {
    removeVariableLocal(variableName);
  }

  public void removeVariableLocal(String variableName) {
    wrappedScope.removeVariableLocal(variableName);
  }

  public void removeVariables(Collection<String> variableNames) {
    removeVariablesLocal(variableNames);
  }

  public void removeVariablesLocal(Collection<String> variableNames) {
    wrappedScope.removeVariablesLocal(variableNames);
  }

  public void removeVariables() {
    removeVariablesLocal();
  }

  public void removeVariablesLocal() {
    wrappedScope.removeVariablesLocal();
  }

}
