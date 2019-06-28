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
package org.camunda.bpm.dmn.engine.impl.transform;

import java.io.File;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRequirementsGraph;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.model.dmn.BuiltinAggregator;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Expression;

public class DmnTransformLogger extends DmnLogger {

  public void decisionTypeNotSupported(Expression expression, Decision decision) {
    logInfo(
      "001",
      "The expression type '{}' of the decision '{}' is not supported. The decision will be ignored.", expression.getClass().getSimpleName(), decision.getName()
    );
  }

  public DmnTransformException unableToTransformDecisionsFromFile(File file, Throwable cause) {
    return new DmnTransformException(exceptionMessage(
      "002",
      "Unable to transform decisions from file '{}'.", file.getAbsolutePath()),
      cause
    );
  }

  public DmnTransformException unableToTransformDecisionsFromInputStream(Throwable cause) {
    return new DmnTransformException(exceptionMessage(
      "003",
      "Unable to transform decisions from input stream."),
      cause
    );
  }

  public DmnTransformException errorWhileTransformingDecisions(Throwable cause) {
    return new DmnTransformException(exceptionMessage(
      "004",
      "Error while transforming decisions: " + cause.getMessage()),
      cause
    );
  }

  public DmnTransformException differentNumberOfInputsAndInputEntries(int inputsSize, int inputEntriesSize, DmnDecisionTableRuleImpl rule) {
    return new DmnTransformException(exceptionMessage(
      "005",
      "The number of inputs '{}' and input entries differ '{}' for rule '{}'.", inputsSize, inputEntriesSize, rule)
    );
  }

  public DmnTransformException differentNumberOfOutputsAndOutputEntries(int outputsSize, int outputEntriesSize, DmnDecisionTableRuleImpl rule) {
    return new DmnTransformException(exceptionMessage(
      "006",
      "The number of outputs '{}' and output entries differ '{}' for rule '{}'.", outputsSize, outputEntriesSize, rule)
    );
  }

  public DmnTransformException hitPolicyNotSupported(DmnDecisionTableImpl decisionTable, HitPolicy hitPolicy, BuiltinAggregator aggregation) {
    if (aggregation == null) {
      return new DmnTransformException(exceptionMessage(
        "007",
        "The hit policy '{}' of decision table '{}' is not supported.", hitPolicy, decisionTable)
      );
    }
    else {
      return new DmnTransformException(exceptionMessage(
        "007",
        "The hit policy '{}' with aggregation '{}' of decision table '{}' is not supported.", hitPolicy, aggregation, decisionTable)
      );
    }
  }

  public DmnTransformException compoundOutputsShouldHaveAnOutputName(DmnDecisionTableImpl dmnDecisionTable, DmnDecisionTableOutputImpl dmnOutput) {
    return new DmnTransformException(exceptionMessage(
      "008",
      "The decision table '{}' has a compound output but output '{}' does not have an output name.", dmnDecisionTable, dmnOutput)
    );
  }

  public DmnTransformException compoundOutputWithDuplicateName(DmnDecisionTableImpl dmnDecisionTable, DmnDecisionTableOutputImpl dmnOutput) {
    return new DmnTransformException(exceptionMessage(
      "009",
      "The decision table '{}' has a compound output but name of output '{}' is duplicate.", dmnDecisionTable, dmnOutput)
    );
  }

  public DmnTransformException decisionIdIsMissing(DmnDecision dmnDecision) {
    return new DmnTransformException(exceptionMessage(
      "010",
      "The decision '{}' must have an 'id' attribute set.", dmnDecision)
    );
  }

  public DmnTransformException decisionTableInputIdIsMissing(DmnDecision dmnDecision, DmnDecisionTableInputImpl dmnDecisionTableInput) {
    return new DmnTransformException(exceptionMessage(
      "011",
      "The decision table input '{}' of decision '{}' must have a 'id' attribute set.", dmnDecisionTableInput, dmnDecision)
    );
  }

  public DmnTransformException decisionTableOutputIdIsMissing(DmnDecision dmnDecision, DmnDecisionTableOutputImpl dmnDecisionTableOutput) {
    return new DmnTransformException(exceptionMessage(
      "012",
      "The decision table output '{}' of decision '{}' must have a 'id' attribute set.", dmnDecisionTableOutput, dmnDecision)
    );
  }

  public DmnTransformException decisionTableRuleIdIsMissing(DmnDecision dmnDecision, DmnDecisionTableRuleImpl dmnDecisionTableRule) {
    return new DmnTransformException(exceptionMessage(
      "013",
      "The decision table rule '{}' of decision '{}' must have a 'id' attribute set.", dmnDecisionTableRule, dmnDecision)
    );
  }

  public void decisionWithoutExpression(Decision decision) {
    logInfo(
      "014",
      "The decision '{}' has no expression and will be ignored.", decision.getName()
    );
  }

  public DmnTransformException requiredDecisionLoopDetected(String decisionId) {
    return new DmnTransformException(exceptionMessage(
      "015",
      "The decision '{}' has a loop.", decisionId)
    );
  }

  public DmnTransformException errorWhileTransformingDefinitions(Throwable cause) {
    return new DmnTransformException(exceptionMessage(
      "016",
      "Error while transforming decision requirements graph: " + cause.getMessage()),
      cause
    );
  }

  public DmnTransformException drdIdIsMissing(DmnDecisionRequirementsGraph drd) {
    return new DmnTransformException(exceptionMessage(
      "017",
      "The decision requirements graph '{}' must have an 'id' attribute set.", drd)
    );
  }

  public DmnTransformException decisionVariableIsMissing(String decisionId) {
    return new DmnTransformException(exceptionMessage(
        "018",
        "The decision '{}' must have an 'variable' element if it contains a literal expression.",
        decisionId));
  }

}
