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

package org.camunda.bpm.engine.impl.scripting.engine;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;


/**
 * <p>A {@link Bindings} implementation which wraps an existing binding and enhances the key / value map with
 * <strong>read-only</strong> access to:
 * <ul>
 * <li>variables provided in a {@link VariableScope},</li>
 * <li>additional bindings provided through a set of {@link Resolver Resolvers}.</li>
 * </ul>
 *
 * <p><strong>Note on backwards compatibility:</strong> before 7.2 the Script
 * bindings behaved in a way that all script variables were automatically exposed
 * as process variables. You can enable this behavior by setting {@link #autoStoreScriptVariables}.
 * </p>
 *
 *
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ScriptBindings implements Bindings {

  /**
   * The script engine implementations put some key/value pairs into the binding.
   * This list contains those keys, such that they wouldn't be stored as process variable.
   *
   * This list contains the keywords for JUEL, Javascript and Groovy.
   */
  protected static final Set<String> UNSTORED_KEYS =
    new HashSet<String>(Arrays.asList(
      "out",
      "out:print",
      "lang:import",
      "context",
      "elcontext",
      "print",
      "println",
      "S", // Spin Internal Variable
      "XML", // Spin Internal Variable
      "JSON", // Spin Internal Variable
      ScriptEngine.ARGV, // jRuby is only setting this variable and execution instead of exporting any other variables
      "execution",
      "__doc__" // do not export python doc string
      ));

  protected List<Resolver> scriptResolvers;
  protected VariableScope variableScope;

  protected Bindings wrappedBindings;

  /** if true, all script variables will be set in the variable scope. */
  protected boolean autoStoreScriptVariables;

  public ScriptBindings(List<Resolver> scriptResolvers, VariableScope variableScope, Bindings wrappedBindings) {
    this.scriptResolvers = scriptResolvers;
    this.variableScope = variableScope;
    this.wrappedBindings = wrappedBindings;
    autoStoreScriptVariables = isAutoStoreScriptVariablesEnabled();
  }

  protected boolean isAutoStoreScriptVariablesEnabled() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if(processEngineConfiguration != null) {
      return processEngineConfiguration.isAutoStoreScriptVariables();
    }
    return false;
  }

  public boolean containsKey(Object key) {
    for (Resolver scriptResolver: scriptResolvers) {
      if (scriptResolver.containsKey(key)) {
        return true;
      }
    }
    return wrappedBindings.containsKey(key);
  }

  public Object get(Object key) {
    Object result = null;

    if(wrappedBindings.containsKey(key)) {
      result = wrappedBindings.get(key);

    } else {
      for (Resolver scriptResolver: scriptResolvers) {
        if (scriptResolver.containsKey(key)) {
          result = scriptResolver.get(key);
        }
      }
    }

    return result;
  }

  public Object put(String name, Object value) {

    if(autoStoreScriptVariables) {
      if (!UNSTORED_KEYS.contains(name)) {
        Object oldValue = variableScope.getVariable(name);
        variableScope.setVariable(name, value);
        return oldValue;
      }
    }

    return wrappedBindings.put(name, value);
  }

  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return calculateBindingMap().entrySet();
  }

  public Set<String> keySet() {
    return calculateBindingMap().keySet();
  }

  public int size() {
    return calculateBindingMap().size();
  }

  public Collection<Object> values() {
    return calculateBindingMap().values();
  }

  public void putAll(Map< ? extends String, ? extends Object> toMerge) {
    for (java.util.Map.Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  public Object remove(Object key) {
    if (UNSTORED_KEYS.contains(key)) {
      return null;
    }
    return wrappedBindings.remove(key);
  }

  public void clear() {
    wrappedBindings.clear();
  }

  public boolean containsValue(Object value) {
    return calculateBindingMap().containsValue(value);
  }

  public boolean isEmpty() {
    return calculateBindingMap().isEmpty();
  }

  protected Map<String, Object> calculateBindingMap() {

    Map<String, Object> bindingMap = new HashMap<String, Object>();

    for (Resolver resolver : scriptResolvers) {
      for (String key : resolver.keySet()) {
        bindingMap.put(key, resolver.get(key));
      }
    }

    Set<java.util.Map.Entry<String, Object>> wrappedBindingsEntries = wrappedBindings.entrySet();
    for (Entry<String, Object> entry : wrappedBindingsEntries) {
      bindingMap.put(entry.getKey(), entry.getValue());
    }

    return bindingMap;
  }

}
