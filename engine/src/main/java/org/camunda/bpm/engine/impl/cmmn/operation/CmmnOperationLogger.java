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
package org.camunda.bpm.engine.impl.cmmn.operation;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;

/**
 * @author Stefan Hentschel.
 */
public class CmmnOperationLogger extends ProcessEngineLogger {

  public void completingSubCaseError(CmmnExecution execution, Throwable cause) {
    logError(
      "001",
      "Error while completing sub case of case execution '{}'. Reason: '{}'",
      execution,
      cause.getMessage(),
      cause);
  }

  public ProcessEngineException completingSubCaseErrorException(CmmnExecution execution, Throwable cause) {
    return new ProcessEngineException(exceptionMessage(
      "002",
      "Error while completing sub case of case execution '{}'.",
      execution
    ), cause);
  }

  public BadUserRequestException exceptionCreateCaseInstanceByIdAndTenantId() {
    return new BadUserRequestException(exceptionMessage(
        "003", "Cannot specify a tenant-id when create a case instance by case definition id."));
  }

}
