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
package org.camunda.bpm.engine.rest.exception;

import javax.ws.rs.core.Response.Status;

/**
 * This exception is used for any kind of errors that occur due to malformed
 * parameters in a Http query.
 * 
 * @author Thorben Lindhauer
 * 
 */
public class InvalidRequestException extends RestException {

  private static final long serialVersionUID = 1L;
  
  public InvalidRequestException(Status status, String message) {
    super(status, message);
  }
  
  public InvalidRequestException(Status status, Exception cause, String message) {
    super(status, cause, message);
  }
}