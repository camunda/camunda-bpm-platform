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
package org.camunda.bpm.dmn.engine.impl;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;

public class DmnEngineLogger extends DmnLogger {

  public DmnTransformException unableToFindDecisionWithKey(String decisionKey) {
    return new DmnTransformException(exceptionMessage(
      "001",
      "Unable to find decision with id '{}' in model.", decisionKey)
    );
  }

  public DmnEvaluationException unableToEvaluateExpression(String expression, String expressionLanguage, Throwable cause) {
    return new DmnEvaluationException(exceptionMessage(
      "002",
      "Unable to evaluate expression for language '{}': '{}'", expressionLanguage, expression),
      cause
    );
  }

  public DmnEvaluationException noScriptEngineFoundForLanguage(String expressionLanguage) {
    return new DmnEvaluationException(exceptionMessage(
      "003",
      "Unable to find script engine for expression language '{}'.", expressionLanguage)
    );
  }

  public DmnEngineException decisionTypeNotSupported(DmnDecision decision) {
    return new DmnEngineException(exceptionMessage(
      "004",
      "Decision type '{}' not supported by DMN engine.", decision.getClass())
    );
  }

  public DmnEngineException invalidValueForTypeDefinition(String typeName, Object value) {
    return new DmnEngineException(exceptionMessage(
      "005",
      "Invalid value '{}' for clause with type '{}'.", value, typeName)
    );
  }

  public void unsupportedTypeDefinitionForClause(String typeName) {
    logWarn(
      "006",
      "Unsupported type '{}' for clause. Values of this clause will not transform into another type.", typeName
    );
  }

  public DmnDecisionResultException decisionOutputHasMoreThanOneValue(DmnDecisionRuleResult ruleResult) {
    return new DmnDecisionResultException(exceptionMessage(
      "007",
      "Unable to get single decision rule result entry as it has more than one entry '{}'", ruleResult)
    );
  }

  public DmnDecisionResultException decisionResultHasMoreThanOneOutput(DmnDecisionTableResult decisionResult) {
    return new DmnDecisionResultException(exceptionMessage(
      "008",
      "Unable to get single decision rule result as it has more than one rule result '{}'", decisionResult)
    );
  }

  public DmnTransformException unableToFindAnyDecisionTable() {
    return new DmnTransformException(exceptionMessage(
      "009",
      "Unable to find any decision table in model.")
    );
  }

  public DmnDecisionResultException decisionOutputHasMoreThanOneValue(DmnDecisionResultEntries result) {
    return new DmnDecisionResultException(exceptionMessage(
      "010",
      "Unable to get single decision result entry as it has more than one entry '{}'", result)
    );
  }

  public DmnDecisionResultException decisionResultHasMoreThanOneOutput(DmnDecisionResult decisionResult) {
    return new DmnDecisionResultException(exceptionMessage(
      "011",
      "Unable to get single decision result as it has more than one result '{}'", decisionResult)
    );
  }

  public DmnEngineException decisionLogicTypeNotSupported(DmnDecisionLogic decisionLogic) {
    return new DmnEngineException(exceptionMessage(
      "012",
      "Decision logic type '{}' not supported by DMN engine.", decisionLogic.getClass())
    );
  }

  public DmnEngineException decisionIsNotADecisionTable(DmnDecision decision) {
    return new DmnEngineException(exceptionMessage(
      "013",
      "The decision '{}' is not implemented as decision table.", decision)
    );
  }

}
