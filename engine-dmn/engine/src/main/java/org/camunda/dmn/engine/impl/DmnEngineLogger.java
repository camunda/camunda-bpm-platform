/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.dmn.engine.impl;

import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.commons.utils.IoUtilException;
import org.camunda.dmn.engine.DmnEngineException;
import org.camunda.dmn.engine.DmnExpressionException;
import org.camunda.dmn.engine.DmnParseException;

public class DmnEngineLogger extends DmnLogger {

  public DmnEngineException outputDoesNotContainAnyComponent() {
    return new DmnEngineException(exceptionMessage("001", "The DMN output doesn't contain any component."));
  }

  public DmnEngineException unableToFindOutputComponentWithName(String name) {
    return new DmnEngineException(exceptionMessage("002", "Unable to find output component with name '{}'.", name));
  }

  public DmnEngineException notConfigurationSetInContext() {
    return new DmnEngineException(exceptionMessage("003", "No engine configuration set in decision context"));
  }

  public DmnEngineException noScriptContextSetInDecisionContext() {
    return new DmnEngineException(exceptionMessage("004", "No script context set in decision context"));
  }

  public DmnEngineException unableToFindScriptEngineForName(String name) {
    return new DmnEngineException(exceptionMessage("005", "Unable to find script engine for name '{}'.", name));
  }

  public DmnParseException unableToReadFile(String filename, Throwable cause) {
    return new DmnParseException(exceptionMessage("006", "Unable to read model from file '{}'.", filename), cause);
  }

  public DmnParseException unableToReadInputStream(Throwable cause) {
    return new DmnParseException(exceptionMessage("007", "Unable to read model from input stream."), cause);
  }

  public DmnParseException unableToFinDecisionWithIdInFile(String filename, String decisionId) {
    return new DmnParseException(exceptionMessage("008", "Unable to find decision with id '{}' in file '{}'.", decisionId, filename));
  }

  public DmnParseException unableToFindDecisionWithId(String decisionId) {
    return new DmnParseException(exceptionMessage("009", "Unable to find decision with id '{}' in model.", decisionId));
  }

  public DmnParseException unableToFindAnyDecisionInFile(String filename) {
    return new DmnParseException(exceptionMessage("010", "Unable to find any decision in file '{}'.", filename));
  }

  public DmnParseException unableToFindAnyDecision() {
    return new DmnParseException(exceptionMessage("011", "Unable to find any decision in model."));
  }

  public DmnEngineException noVariableContextSetInDecisionContext() {
    return new DmnEngineException(exceptionMessage("012", "No variable context set in decision context"));
  }

  public DmnExpressionException unableToEvaluateExpression(String expression, String expressionLanguage, Throwable cause) {
    return new DmnExpressionException(exceptionMessage("013", "Unable to evaluate expression for language '{}': '{}'", expressionLanguage, expression), cause);
  }

  public DmnExpressionException unableToCastExpressionResult(Object result, Throwable cause) {
    return new DmnExpressionException(exceptionMessage("014", "Unable to cast result '{}' to expected type", result), cause);
  }

}
