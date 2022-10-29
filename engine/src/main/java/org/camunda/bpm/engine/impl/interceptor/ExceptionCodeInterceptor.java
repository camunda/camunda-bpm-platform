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
package org.camunda.bpm.engine.impl.interceptor;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode;
import org.camunda.bpm.engine.impl.errorcode.ExceptionCodeProvider;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;

import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * <p>A command interceptor to catch {@link ProcessEngineException} errors and assign error codes.
 *
 * <p>The interceptor assigns an error code to the {@link ProcessEngineException}
 * based on the built-in or custom {@link ExceptionCodeProvider}.
 */
public class ExceptionCodeInterceptor extends CommandInterceptor {

  protected static final CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  public static final int MIN_CUSTOM_CODE = 20_000;
  public static final int MAX_CUSTOM_CODE = 39_999;

  protected ExceptionCodeProvider builtinExceptionCodeProvider;
  protected ExceptionCodeProvider customExceptionCodeProvider;

  public ExceptionCodeInterceptor(ExceptionCodeProvider builtinExceptionCodeProvider,
                                  ExceptionCodeProvider customExceptionCodeProvider) {
    this.builtinExceptionCodeProvider = builtinExceptionCodeProvider;
    this.customExceptionCodeProvider = customExceptionCodeProvider;
  }

  @Override
  public <T> T execute(Command<T> command) {
    try {
      return next.execute(command);

    } catch (ProcessEngineException pex) {
      assignCodeToException(pex);
      throw pex;

    }
  }

  /**
   * <p>Built-in code provider has precedence over custom code provider and initial code (assigned via delegation code).
   * Custom and initial code is tried to be reset in case it violates the reserved code range.
   *
   * <p>When {@code disableBuiltInExceptionCodeProvider} flag
   * in {@link ProcessEngineConfigurationImpl} is configured to {@code true},
   * custom provider can override reserved codes.
   */
  protected Integer provideCodeBySupplier(Supplier<Integer> builtinSupplier,
                                          Supplier<Integer> customSupplier,
                                          int initialCode) {
    boolean assignedByDelegationCode = initialCode != BuiltinExceptionCode.FALLBACK.getCode();
    boolean builtinProviderConfigured = builtinExceptionCodeProvider != null;

    if (builtinProviderConfigured) {
      Integer providedCode = builtinSupplier.get();
      if (providedCode != null) {
        if (assignedByDelegationCode) {
          LOG.warnResetToBuiltinCode(providedCode, initialCode);
        }
        return providedCode;
      }

    }

    boolean customProviderConfigured = customExceptionCodeProvider != null;
    if (customProviderConfigured && !assignedByDelegationCode) {
      Integer providedCode = customSupplier.get();
      if (providedCode != null && builtinProviderConfigured) {
        return tryResetReservedCode(providedCode);

      } else {
        return providedCode;

      }

    } else if (builtinProviderConfigured) {
      return tryResetReservedCode(initialCode);

    }

    return null;
  }

  protected Integer provideCode(ProcessEngineException pex, int initialCode) {
    SQLException sqlException = ExceptionUtil.unwrapException(pex);

    Supplier<Integer> builtinSupplier = null;
    Supplier<Integer> customSupplier = null;
    if (sqlException != null) {
      builtinSupplier = () -> builtinExceptionCodeProvider.provideCode(sqlException);
      customSupplier = () -> customExceptionCodeProvider.provideCode(sqlException);

    } else {
      builtinSupplier = () -> builtinExceptionCodeProvider.provideCode(pex);
      customSupplier = () -> customExceptionCodeProvider.provideCode(pex);

    }

    return provideCodeBySupplier(builtinSupplier, customSupplier, initialCode);
  }

  /**
   * Resets codes to the {@link BuiltinExceptionCode#FALLBACK}
   * in case they are < {@link #MIN_CUSTOM_CODE} or > {@link #MAX_CUSTOM_CODE}.
   * No log is written when code is {@link BuiltinExceptionCode#FALLBACK}.
   */
  protected Integer tryResetReservedCode(Integer code) {
    if (codeReserved(code)) {
      LOG.warnReservedErrorCode(code);
      return BuiltinExceptionCode.FALLBACK.getCode();

    } else {
      return code;

    }
  }

  protected boolean codeReserved(Integer code) {
    return code != null && code != BuiltinExceptionCode.FALLBACK.getCode() &&
        (code < MIN_CUSTOM_CODE || code > MAX_CUSTOM_CODE);
  }

  protected void assignCodeToException(ProcessEngineException pex) {
    int initialCode = pex.getCode();
    Integer providedCode = provideCode(pex, initialCode);

    if (providedCode != null) {
      pex.setCode(providedCode);

    }
  }

}