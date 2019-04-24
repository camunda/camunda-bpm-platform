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
package org.camunda.bpm.engine.rest.exception;

import javax.ws.rs.core.Response.Status;

public class RestException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;
  
  private Status status;
  
  public RestException(String message) {
    super(message);
  }
  
  public RestException(Status status, String message) {
    super(message);
    this.status = status;
  }
  
  public RestException(Status status, Exception cause) {
    super(cause);
    this.status = status;
  }
  
  public RestException(Status status, Exception cause, String message) {
    super(message, cause);
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }
}
