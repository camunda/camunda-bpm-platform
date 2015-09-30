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

package org.camunda.bpm.dmn.engine.impl;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.DmnExpressionException;
import org.camunda.bpm.dmn.engine.DmnTransformException;
import org.camunda.bpm.model.dmn.HitPolicy;

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

  public DmnTransformException unableToReadFile(String filename, Throwable cause) {
    return new DmnTransformException(exceptionMessage("006", "Unable to read model from file '{}'.", filename), cause);
  }

  public DmnTransformException unableToReadInputStream(Throwable cause) {
    return new DmnTransformException(exceptionMessage("007", "Unable to read model from input stream."), cause);
  }

  public DmnTransformException unableToFinDecisionWithKeyInFile(String filename, String decisionKey) {
    return new DmnTransformException(exceptionMessage("008", "Unable to find decision with id '{}' in file '{}'.", decisionKey, filename));
  }

  public DmnTransformException unableToFindDecisionWithKey(String decisionKey) {
    return new DmnTransformException(exceptionMessage("009", "Unable to find decision with id '{}' in model.", decisionKey));
  }

  public DmnTransformException unableToFindAnyDecisionInFile(String filename) {
    return new DmnTransformException(exceptionMessage("010", "Unable to find any decision in file '{}'.", filename));
  }

  public DmnTransformException unableToFindAnyDecision() {
    return new DmnTransformException(exceptionMessage("011", "Unable to find any decision in model."));
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

  public DmnExpressionException noScriptEngineFoundForLanguage(String expressionLanguage) {
    return new DmnExpressionException(exceptionMessage("015", "Unable to find script engine for expression language '{}'.", expressionLanguage));
  }

  public DmnEngineException decisionTypeNotSupported(DmnDecision decision) {
    return new DmnEngineException(exceptionMessage("016", "Decision type '{}' not supported by DMN engine.", decision.getClass()));
  }

  public DmnEngineException unableToFindHitPolicyHandlerFor(HitPolicy hitPolicy) {
    return new DmnEngineException(exceptionMessage("017", "Unable to find handler for hit policy '{}'.", hitPolicy));
  }

  public DmnEngineException invalidValueForTypeDefinition(String typeName, Object value) {
    return new DmnEngineException(exceptionMessage("018", "Invalid value '{}' for clause with type '{}'.", value, typeName));
  }

  public void unsupportedTypeDefinitionForClause(String typeName) {
    logWarn("019", "Unsupported type '{}' for clause. Values of this clause will not transform into another type.", typeName);
  }

}
