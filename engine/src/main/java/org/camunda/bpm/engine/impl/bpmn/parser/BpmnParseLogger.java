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
package org.camunda.bpm.engine.impl.bpmn.parser;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.io.StreamSource;

/**
 * @author Stefan Hentschel.
 */
public class BpmnParseLogger extends ProcessEngineLogger {

  // LOGGING

  public void parsingElement(String elementType, String elementId) {
    logDebug("001", "Parsing element from type '{}' with id '{}'", elementType, elementId);
  }

  public void ignoringNonExecutableProcess(String elementId) {
    logInfo("002", "Ignoring non-executable process with id '{}'. Set the attribute isExecutable=\"true\" to deploy " +
      "this process.", elementId);
  }

  public void missingIsExecutableAttribute(String elementId) {
    logInfo("003", "Process with id '{}' has no attribute isExecutable. Better set the attribute explicitly, " +
      "especially to be compatible with future engine versions which might change the default behavior.", elementId);
  }

  public void parsingFailure(Throwable cause) {
    logError("004", "Unexpected Exception with message: {} ", cause.getMessage());
  }

  public void unableToSetSchemaResource(Throwable cause) {
    logWarn("005", "Setting schema resource failed because of: '{}'", cause.getMessage());
  }


  // EXCEPTIONS

  public ProcessEngineException malformedUrlException(String url, Throwable cause) {
    return new ProcessEngineException(exceptionMessage("006", "The URL '{}' is malformed", url), cause);
  }

  public ProcessEngineException multipleSourcesException(StreamSource source1, StreamSource source2) {
    return new ProcessEngineException(exceptionMessage(
      "007",
      "Multiple sources detected, which is invalid. Source 1: '{}', Source 2: {}",
      source1,
      source2
    ));
  }

  public ProcessEngineException parsingFailureException(String name, Throwable cause) {
    return new ProcessEngineException(exceptionMessage("008", "Could not parse '{}'.", name), cause);
  }

  public ProcessEngineException parsingProcessException(Exception cause) {
    return new ProcessEngineException(exceptionMessage("009", "Error while parsing process"), cause);
  }

}
