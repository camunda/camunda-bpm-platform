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
package org.camunda.bpm.client.exception;

/**
 * Thrown when a request from the engine's REST API fails.
 */
public class RestException extends ExternalTaskClientException {

  protected Integer httpStatusCode;
  protected String type;
  protected Integer code;

  public RestException(String message, String type, Integer code) {
    super(message);
    this.type = type;
    this.code = code;
  }

  public RestException(String message, Throwable throwable) {
    super(message, throwable);
  }

  /**
   * @return the http status code from the Engine's REST API.
   */
  public Integer getHttpStatusCode() {
    return getCause() == null ? httpStatusCode : ((RestException) getCause()).getHttpStatusCode();
  }

  public void setHttpStatusCode(Integer httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
  }

  /**
   * @return the exception type from the Engine's REST API.
   */
  public String getType() {
    return getCause() == null ? type : ((RestException) getCause()).getType();
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the exception error code from the Engine's REST API.
   */
  public Integer getCode() {
    return getCause() == null ? code : ((RestException) getCause()).getCode();
  }

  public void setCode(Integer code) {
    this.code = code;
  }

}
