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

import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.dmn.engine.DmnParseException;

public class DmnParseLogger extends DmnLogger {

  public DmnParseException unableToFindDecision(String decisionId, String filename) {
    String id = "001";
    String message;
    if (decisionId != null && !decisionId.isEmpty() && filename != null) {
      message = exceptionMessage(id, "Unable to find decision '{}' in DMN model '{}'.", decisionId, filename);
    }
    else if (decisionId != null && !decisionId.isEmpty()) {
      message = exceptionMessage(id, "Unable to find decision '{}' in DMN model.", decisionId);

    }
    else if (filename != null) {
      message = exceptionMessage(id, "Unable to find decision in DMN model '{}'.", filename);
    }
    else {
      message = exceptionMessage(id, "Unable to find decision in DMN model.");
    }

    return new DmnParseException(message);
  }

  public void decisionTypeNotSupported(Decision decision) {
    logInfo("002", "The expression type '{}' of the decision '{}' is not supported.", decision.getClass().getSimpleName(), decision.getId());
  }

  public DmnParseException unableToParseDecisionFromFile(String filename, String decisionId, Throwable cause) {
    String id = "003";
    if (decisionId != null && !decisionId.isEmpty()) {
      return new DmnParseException(exceptionMessage(id, "Unable to parse decision '{}' from file '{}'.", decisionId, filename), cause);
    }
    else {
      return new DmnParseException(exceptionMessage(id, "Unable to parse decision from file '{}'.", filename), cause);
    }
  }

  public void ignoringClause(Clause clause) {
    logInfo("004", "Ignoring clause '{}' as neither an input expression nor output definition was found.", clause.getId());
  }

}
