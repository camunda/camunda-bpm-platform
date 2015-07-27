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

import java.io.File;

import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.dmn.engine.DmnTransformException;

public class DmnTransformLogger extends DmnLogger {

  public DmnTransformException unableToFindDecision(String decisionKey, String filename) {
    String id = "001";
    String message;
    if (decisionKey != null && !decisionKey.isEmpty() && filename != null) {
      message = exceptionMessage(id, "Unable to find decision '{}' in DMN model '{}'.", decisionKey, filename);
    }
    else if (decisionKey != null && !decisionKey.isEmpty()) {
      message = exceptionMessage(id, "Unable to find decision '{}' in DMN model.", decisionKey);

    }
    else if (filename != null) {
      message = exceptionMessage(id, "Unable to find decision in DMN model '{}'.", filename);
    }
    else {
      message = exceptionMessage(id, "Unable to find decision in DMN model.");
    }

    return new DmnTransformException(message);
  }

  public void decisionTypeNotSupported(Decision decision) {
    logInfo("002", "The expression type '{}' of the decision '{}' is not supported.", decision.getClass().getSimpleName(), decision.getId());
  }

  public DmnTransformException unableToTransformDecisionFromFile(String filename, String decisionKey, Throwable cause) {
    String id = "003";
    if (decisionKey != null && !decisionKey.isEmpty()) {
      return new DmnTransformException(exceptionMessage(id, "Unable to transform decision '{}' from file '{}'.", decisionKey, filename), cause);
    }
    else {
      return new DmnTransformException(exceptionMessage(id, "Unable to transform decision from file '{}'.", filename), cause);
    }
  }

  public void ignoringClause(Clause clause) {
    logInfo("004", "Ignoring clause '{}' as neither an input expression nor output definition was found.", clause.getId());
  }

  public DmnTransformException unableToTransformModelFromFile(File file, Throwable cause) {
    return new DmnTransformException(exceptionMessage("005", "Unable to transform decision model from file '{}'.", file.getAbsolutePath()), cause);
  }

  public DmnTransformException unableToTransformModelFromInputStream(Throwable cause) {
    return new DmnTransformException(exceptionMessage("006", "Unable to transform decision model from input stream."), cause);
  }

  public DmnTransformException errorWhileTransforming(Throwable cause) {
    return new DmnTransformException(exceptionMessage("007", "Error while transforming model: " + cause.getMessage() ), cause);
  }

  public DmnTransformException noElementHandlerForClassRegistered(Class<?> elementClass) {
    return new DmnTransformException(exceptionMessage("008", "No element handler for element class '{}' registered.", elementClass));
  }

  public DmnTransformException unableToFindInputEntry(String inputEntryKey) {
    return new DmnTransformException(exceptionMessage("009", "Unable to find input entry with id '{}'.", inputEntryKey));
  }

  public DmnTransformException unableToFindOutputEntry(String outputEntryKey) {
    return new DmnTransformException(exceptionMessage("009", "Unable to find output entry with id '{}'.", outputEntryKey));
  }

}
