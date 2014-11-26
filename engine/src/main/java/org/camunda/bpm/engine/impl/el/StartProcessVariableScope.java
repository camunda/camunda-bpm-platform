/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.el;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * Variable-scope only used to resolve variables when NO execution is active but
 * expression-resolving is needed. This occurs eg. when start-form properties have default's
 * defined. Even though variables are not available yet, expressions should be resolved
 * anyway.
 *
 * @author Frederik Heremans
 */
public class StartProcessVariableScope implements VariableScope {

  private static final StartProcessVariableScope INSTANCE = new StartProcessVariableScope();

  private static VariableMap EMPTY_VARIABLE_MAP = Variables.fromMap(Collections.<String, Object>emptyMap());

  /**
   * Since a {@link StartProcessVariableScope} has no state, it's safe to use the same
   * instance to prevent too many useless instances created.
   */
  public static StartProcessVariableScope getSharedInstance()  {
    return INSTANCE;
  }

  public String getVariableScopeKey() {
    return "scope";
  }

  public VariableMap getVariables() {
    return EMPTY_VARIABLE_MAP;
  }

  public VariableMap getVariablesLocal() {
    return EMPTY_VARIABLE_MAP;
  }

  public Object getVariable(String variableName) {
    return null;
  }

  public Object getVariableLocal(String variableName) {
    return null;
  }

  public VariableMap getVariablesTyped(boolean deserializeObjectValues) {
    return getVariables();
  }

  public VariableMap getVariablesLocalTyped() {
    return getVariablesLocalTyped(true);
  }

  public VariableMap getVariablesTyped() {
    return getVariablesTyped(true);
  }

  public VariableMap getVariablesLocalTyped(boolean deserializeObjectValues) {
    return getVariablesLocal();
  }

  public Object getVariable(String variableName, boolean deserializeObjectValue) {
    return null;
  }

  public Object getVariableLocal(String variableName, boolean deserializeObjectValue) {
    return null;
  }

  public <T extends TypedValue> T getVariableTyped(String variableName) {
    return null;
  }

  public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeObjectValue) {
    return null;
  }

  public <T extends TypedValue> T getVariableLocalTyped(String variableName) {
    return null;
  }

  public <T extends TypedValue> T getVariableLocalTyped(String variableName, boolean deserializeObjectValue) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public Set<String> getVariableNames() {
    return Collections.EMPTY_SET;
  }

  public Set<String> getVariableNamesLocal() {
    return null;
  }

  public void setVariable(String variableName, Object value) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  public void setVariableLocal(String variableName, Object value) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  public void setVariables(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  public boolean hasVariables() {
    return false;
  }

  public boolean hasVariablesLocal() {
    return false;
  }

  public boolean hasVariable(String variableName) {
    return false;
  }

  public boolean hasVariableLocal(String variableName) {
    return false;
  }

  public void removeVariable(String variableName) {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  public void removeVariableLocal(String variableName) {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  public void removeVariables() {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  public void removeVariablesLocal() {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  public void removeVariables(Collection<String> variableNames) {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  public void removeVariablesLocal(Collection<String> variableNames) {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  public Map<String, CoreVariableInstance> getVariableInstances() {
    return Collections.emptyMap();
  }

  public CoreVariableInstance getVariableInstance(String name) {
    return null;
  }

  public Map<String, CoreVariableInstance> getVariableInstancesLocal() {
    return Collections.emptyMap();
  }

  public CoreVariableInstance getVariableInstanceLocal(String name) {
    return null;
  }

}
