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
package org.camunda.bpm.engine.impl.core.variable.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class VariableStore<T extends CoreVariableInstance> {

  protected VariablesProvider<T> variablesProvider;
  protected Map<String, T> variables;

  protected List<VariableStoreObserver<T>> observers;

  public VariableStore() {
    this(VariableCollectionProvider.<T>emptyVariables());
  }

  public VariableStore(VariablesProvider<T> provider, VariableStoreObserver<T>... observers) {
    this.variablesProvider = provider;
    this.observers = new ArrayList<VariableStoreObserver<T>>();
    this.observers.addAll(Arrays.asList(observers));
  }

  /**
   * The variables provider can be exchanged as long as the variables are not yet initialized
   */
  public void setVariablesProvider(VariablesProvider<T> variablesProvider) {
    if (variables != null) {
      // already initialized
      return;
    }
    else {
      this.variablesProvider = variablesProvider;
    }

  }

  protected Map<String, T> getVariablesMap() {
    forceInitialization();

    return variables;
  }

  public T getVariable(String name) {

    return getVariablesMap().get(name);
  }

  public List<T> getVariables() {
    return new ArrayList<T>(getVariablesMap().values());
  }


  public void addVariable(T value) {

    if (containsKey(value.getName())) {
      throw ProcessEngineLogger.CORE_LOGGER.duplicateVariableInstanceException(value);
    }

    getVariablesMap().put(value.getName(), value);

    for (VariableStoreObserver<T> listener : observers) {
      listener.onAdd(value);
    }
  }

  public void updateVariable(T value)
  {
    if (!containsKey(value.getName()))
    {
      throw ProcessEngineLogger.CORE_LOGGER.duplicateVariableInstanceException(value);
    }
  }

  public boolean isEmpty() {
    return getVariablesMap().isEmpty();
  }

  public boolean containsValue(T value) {
    return getVariablesMap().containsValue(value);
  }

  public boolean containsKey(String key) {
    return getVariablesMap().containsKey(key);
  }

  public Set<String> getKeys() {
    return new HashSet<String>(getVariablesMap().keySet());
  }

  public boolean isInitialized() {
    return variables != null;
  }

  public void forceInitialization() {
    if (!isInitialized()) {
      variables = new HashMap<String, T>();

      for (T variable : variablesProvider.provideVariables()) {
        variables.put(variable.getName(), variable);
      }
    }
  }

  public T removeVariable(String variableName) {

    if (!getVariablesMap().containsKey(variableName)) {
      return null;
    }

    T value = getVariablesMap().remove(variableName);

    for (VariableStoreObserver<T> observer : observers) {
      observer.onRemove(value);
    }

    return value;
  }

  public void removeVariables() {
    Iterator<T> valuesIt = getVariablesMap().values().iterator();

    while (valuesIt.hasNext()) {
      T nextVariable = valuesIt.next();

      valuesIt.remove();

      for (VariableStoreObserver<T> observer : observers) {
        observer.onRemove(nextVariable);
      }
    }
  }

  public void addObserver(VariableStoreObserver<T> observer) {
    observers.add(observer);
  }

  public void removeObserver(VariableStoreObserver<T> observer) {
    observers.remove(observer);
  }

  public static interface VariableStoreObserver<T extends CoreVariableInstance> {

    void onAdd(T variable);

    void onRemove(T variable);
  }

  public static interface VariablesProvider<T extends CoreVariableInstance> {

    Collection<T> provideVariables();

  }

}
