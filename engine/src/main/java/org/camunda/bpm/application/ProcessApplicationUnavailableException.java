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
package org.camunda.bpm.application;

/**
 * <p>Checked exception thrown by a {@link ProcessApplicationReference} if the referenced 
 * process application is unavailable.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationUnavailableException extends Exception {

  private static final long serialVersionUID = 1L;

  public ProcessApplicationUnavailableException() {
    super();
  }

  public ProcessApplicationUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcessApplicationUnavailableException(String message) {
    super(message);
  }

  public ProcessApplicationUnavailableException(Throwable cause) {
    super(cause);
  }

}
