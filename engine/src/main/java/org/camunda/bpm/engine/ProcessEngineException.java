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
package org.camunda.bpm.engine;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode;
import org.camunda.bpm.engine.impl.errorcode.ExceptionCodeProvider;

/**
 * Runtime exception that is the superclass of all exceptions in the process engine.
 *
 * @author Tom Baeyens
 */
public class ProcessEngineException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  protected int code = BuiltinExceptionCode.FALLBACK.getCode();

  public ProcessEngineException() {
    super();
  }

  public ProcessEngineException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcessEngineException(String message) {
    super(message);
  }

  public ProcessEngineException(String message, int code) {
    super(message);
    this.code = code;
  }

  public ProcessEngineException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>The exception code can be set via delegation code.
   *
   * <p>Setting an error code on the exception in delegation code always overrides
   * the exception code from a custom {@link ExceptionCodeProvider}.
   *
   * <p>Your business logic can react to the exception code exposed
   * via {@link #getCode} when calling Camunda Java API and is
   * even exposed to the REST API when an error occurs.
   */
  public void setCode(int code) {
    this.code = code;
  }

  /**
   * <p>Accessor of the exception error code.
   *
   * <p>If not changed via {@link #setCode}, default code is {@link BuiltinExceptionCode#FALLBACK}
   * which is always overridden by a custom or built-in error code provider.
   *
   * <p>You can implement a custom {@link ExceptionCodeProvider}
   * and register it in the {@link ProcessEngineConfigurationImpl}
   * via the {@code customExceptionCodeProvider} property.
   */
  public int getCode() {
    return code;
  }

}
