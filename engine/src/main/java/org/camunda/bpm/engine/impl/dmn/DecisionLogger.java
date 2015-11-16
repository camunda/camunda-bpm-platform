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
package org.camunda.bpm.engine.impl.dmn;

import java.util.Collection;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

/**
 * @author Roman Smirnov
 *
 */
public class DecisionLogger extends ProcessEngineLogger {

  public ProcessEngineException decisionResultMappingException(DmnDecisionTableResult decisionResult, DmnEngineException cause) {
    return new ProcessEngineException(exceptionMessage(
        "001",
        "The decision result mapper failed to process '{}'",
        decisionResult
      ), cause);
  }

  public ProcessEngineException decisionResultCollectMappingException(Collection<String> outputNames, DmnDecisionTableResult decisionResult) {
    return new ProcessEngineException(exceptionMessage(
        "002",
        "The decision result mapper failed to process '{}'. The decision outputs should only contains values for one output name but found '{}'.",
        decisionResult,
        outputNames
      ));
  }

}
