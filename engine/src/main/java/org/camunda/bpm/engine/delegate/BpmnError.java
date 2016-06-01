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

package org.camunda.bpm.engine.delegate;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.parser.Error;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;


/**
 * Special exception that can be used to throw a BPMN Error from
 * {@link JavaDelegate}s and expressions.
 * 
 * This should only be used for business faults, which shall be handled by a
 * Boundary Error Event or Error Event Sub-Process modeled in the process
 * definition. Technical errors should be represented by other exception types.
 * 
 * This class represents an actual instance of a BPMN Error, whereas
 * {@link Error} represents an Error definition.
 * 
 * @author Falko Menge
 */
public class BpmnError extends ProcessEngineException {
  
  private static final long serialVersionUID = 1L;

  private String errorCode;
  private String errorMessage;

  public BpmnError(String errorCode) {
    super("");
    setErrorCode(errorCode);
  }
          
  public BpmnError(String errorCode, String message) {
    super(message + " (errorCode='" + errorCode + "')");
    setErrorCode(errorCode);
    setMessage(message);
  }

  protected void setErrorCode(String errorCode) {
    ensureNotEmpty("Error Code", errorCode);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String toString() {
    return super.toString() + " (errorCode='" + errorCode + "')";
  }

  protected void setMessage(String errorMessage) {
    ensureNotEmpty("Error Message", errorMessage);
    this.errorMessage = errorMessage;
  }

  public String getMessage() {
    return errorMessage;
  }
}
