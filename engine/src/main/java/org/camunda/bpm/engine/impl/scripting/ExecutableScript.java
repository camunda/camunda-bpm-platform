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
package org.camunda.bpm.engine.impl.scripting;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.camunda.bpm.dmn.engine.DmnDecisionOutput;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.scriptengine.DmnScriptEngineFactory;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.VariableScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>Represents an executable script.</p>
 *
 *
 * @author Daniel Meyer
 *
 */
public abstract class ExecutableScript {

  /** The language of the script. Used to resolve the
   * {@link ScriptEngine}. */
  protected final String language;

  protected ExecutableScript(String language) {
    this.language = language;
  }

  /**
   * The language in which the script is written.
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * <p>Evaluates the script using the provided engine and bindings</p>
   *
   * @param scriptEngine the script engine to use for evaluating the script.
   * @param variableScope the variable scope of the execution
   * @param bindings the bindings to use for evaluating the script.
   * @throws ProcessEngineException in case the script cannot be evaluated.
   * @return the result of the script evaluation
   */
  public Object execute(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings) {
    Object result = evaluate(scriptEngine, variableScope, bindings);
    return postProcessResult(result);
  }

  protected abstract Object evaluate(ScriptEngine scriptEngine, VariableScope variableScope, Bindings bindings);

  public Object postProcessResult(Object result) {
    if (result != null && DmnScriptEngineFactory.names.contains(language)) {
      return postProcessDmnResult((DmnDecisionResult) result);
    }
    else {
      // do nothing
      return result;
    }
  }

  protected Object postProcessDmnResult(DmnDecisionResult result) {
    if (result.isEmpty()) {
      // the result contained no output
      return null;
    }
    else if (result.size() == 1) {
      // the result contained one output
      return unpackDecisionOutput(result.get(0));
    }
    else {
      // the result contained multiple output
      return unpackDecisionOutputs(result);
    }
  }

  protected Object unpackDecisionOutput(DmnDecisionOutput output) {
    if (output.isEmpty()) {
      // the output contained no entries
      return Collections.<String, Object>emptyMap();
    }
    else if (output.size() == 1) {
      // the output contained one entry
      return output.getValue();
    }
    else {
      // the output contained multiple entries
      return output;
    }
  }

  @SuppressWarnings("unchecked")
  protected Object unpackDecisionOutputs(DmnDecisionResult result) {
    // determine maximal number of entries for single output
    int entriesCount = 0;
    for (DmnDecisionOutput output : result) {
      int size = output.size();
      if (size > entriesCount) {
        entriesCount = size;
      }
    }

    if (entriesCount == 0) {
      // all output contained no entry
      List<Map<String, Object>> nullList = new ArrayList<Map<String, Object>>();
      for (DmnDecisionOutput output : result) {
        nullList.add(null);
      }
      return nullList;
    }
    else if (entriesCount == 1) {
      // every output contained maximal one entry => return list of entries
      return createDecisionOutputList(result);
    }
    else {
      // it exists outputs with multiple entries => return full result
      return result;
    }
  }

  protected List<Object> createDecisionOutputList(List<DmnDecisionOutput> result) {
    List<Object> outputList = new ArrayList<Object>();
    for (DmnDecisionOutput output : result) {
      outputList.add(output.getValue());
    }
    return outputList;
  }

}
