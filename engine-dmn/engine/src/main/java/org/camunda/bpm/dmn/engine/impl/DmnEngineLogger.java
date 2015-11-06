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
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;

public class DmnEngineLogger extends DmnLogger {

  public DmnTransformException unableToReadFile(String filename, Throwable cause) {
    return new DmnTransformException(exceptionMessage(
      "001",
      "Unable to read model from file '{}'.", filename),
      cause
    );
  }

  public DmnTransformException unableToFindDecisionWithKeyInFile(String decisionKey, String filename) {
    return new DmnTransformException(exceptionMessage(
      "002",
      "Unable to find decision with id '{}' in file '{}'.", decisionKey, filename)
    );
  }

  public DmnTransformException unableToFindDecisionWithKey(String decisionKey) {
    return new DmnTransformException(exceptionMessage(
      "003",
      "Unable to find decision with id '{}' in model.", decisionKey)
    );
  }

  public DmnTransformException unableToFindAnyDecisionInFile(String filename) {
    return new DmnTransformException(exceptionMessage(
      "004",
      "Unable to find any decision in file '{}'.", filename)
    );
  }

  public DmnTransformException unableToFindAnyDecision() {
    return new DmnTransformException(exceptionMessage(
      "005",
      "Unable to find any decision in model.")
    );
  }

  public DmnEvaluationException unableToEvaluateExpression(String expression, String expressionLanguage, Throwable cause) {
    return new DmnEvaluationException(exceptionMessage(
      "006",
      "Unable to evaluate expression for language '{}': '{}'", expressionLanguage, expression),
      cause
    );
  }

  public DmnEvaluationException noScriptEngineFoundForLanguage(String expressionLanguage) {
    return new DmnEvaluationException(exceptionMessage(
      "007",
      "Unable to find script engine for expression language '{}'.", expressionLanguage)
    );
  }

  public DmnEngineException decisionTypeNotSupported(DmnDecision decision) {
    return new DmnEngineException(exceptionMessage(
      "008",
      "Decision type '{}' not supported by DMN engine.", decision.getClass())
    );
  }

  public DmnEngineException invalidValueForTypeDefinition(String typeName, Object value) {
    return new DmnEngineException(exceptionMessage(
      "009",
      "Invalid value '{}' for clause with type '{}'.", value, typeName)
    );
  }

  public void unsupportedTypeDefinitionForClause(String typeName) {
    logWarn(
      "010",
      "Unsupported type '{}' for clause. Values of this clause will not transform into another type.", typeName
    );
  }

  public DmnDecisionResultException decisionOutputHasMoreThanOneValue(DmnDecisionRuleResult ruleResult) {
    return new DmnDecisionResultException(exceptionMessage(
      "011",
      "Unable to get single decision rule result entry as it has more than one entry '{}'", ruleResult)
    );
  }

  public DmnDecisionResultException decisionResultHasMoreThanOneOutput(DmnDecisionTableResult decisionResult) {
    return new DmnDecisionResultException(exceptionMessage(
      "012",
      "Unable to get single decision rule result as it has more than one rule result '{}'", decisionResult)
    );
  }

  public DmnTransformException unableToFindAnyDecisionTableInFile(String filename) {
    return new DmnTransformException(exceptionMessage(
      "013",
      "Unable to find any decision table in file '{}'.", filename)
    );
  }

  public DmnTransformException unableToFindAnyDecisionTable() {
    return new DmnTransformException(exceptionMessage(
      "014",
      "Unable to find any decision table in model.")
    );
  }

}
