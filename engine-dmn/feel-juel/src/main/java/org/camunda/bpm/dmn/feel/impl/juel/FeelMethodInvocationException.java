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
package org.camunda.bpm.dmn.feel.impl.juel;

import org.camunda.bpm.dmn.feel.impl.FeelException;

/**
 * Exception thrown if an error occurs during a method invocation
 * in a FEEL expression.
 */
public class FeelMethodInvocationException extends FeelException {

  protected String method;
  protected String[] parameters;

  public FeelMethodInvocationException(String message, String method, String... parameters) {
    super(message);
    this.method = method;
    this.parameters = parameters;
  }

  public FeelMethodInvocationException(String message, Throwable cause, String method, String... parameters) {
    super(message, cause);
    this.method = method;
    this.parameters = parameters;
  }

  public String getMethod() {
    return method;
  }

  public String[] getParameters() {
    return parameters;
  }

}
