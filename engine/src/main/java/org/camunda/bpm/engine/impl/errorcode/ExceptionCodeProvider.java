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
package org.camunda.bpm.engine.impl.errorcode;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;

import java.sql.SQLException;

import static org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode.COLUMN_SIZE_TOO_SMALL;
import static org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode.DEADLOCK;
import static org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode.FOREIGN_KEY_CONSTRAINT_VIOLATION;
import static org.camunda.bpm.engine.impl.errorcode.BuiltinExceptionCode.OPTIMISTIC_LOCKING;

/**
 * <p>One of the provider methods are called when a {@link ProcessEngineException} occurs.
 * The default implementation provides the built-in exception codes.
 *
 * <p>You can disable the built-in or/and additionally register a custom provider via
 * the {@link ProcessEngineConfigurationImpl} using the following properties:
 * <ul>
 *   <li>{@code disableExceptionCode} - disables the whole feature
 *   <li>{@code disableBuiltinExceptionCodeProvider} - only disables the built-in provider
 *   and allows overriding reserved exception codes
 *   <li>{@code customExceptionCodeProvider} - provide custom exception codes
 */
public interface ExceptionCodeProvider {

  /**
   * <p>Called when a {@link ProcessEngineException} occurs.
   *
   * <p>Provides the exception code that can be determined based on the passed {@link ProcessEngineException}.
   * Only called when no other provider method is called.
   *
   * @param processEngineException that occurred.
   * @return an integer value representing the error code. When returning {@code null},
   * the {@link BuiltinExceptionCode#FALLBACK} gets assigned to the exception.
   */
  default Integer provideCode(ProcessEngineException processEngineException) {
    if (processEngineException instanceof OptimisticLockingException) {
      return OPTIMISTIC_LOCKING.getCode();

    }

    return null;
  }

  /**
   * <p>Called when a {@link SQLException} occurs.
   *
   * <p>Provides the exception code that can be determined based on the passed {@link SQLException}.
   * The error code is assigned to the top level {@link ProcessEngineException}.
   * Only called when no other provider method is called.
   *
   * @param sqlException that occurred.
   * @return an integer value representing the error code. When returning {@code null},
   * the {@link BuiltinExceptionCode#FALLBACK} gets assigned to the exception.
   */
  default Integer provideCode(SQLException sqlException) {
    boolean deadlockDetected = ExceptionUtil.checkDeadlockException(sqlException);
    if (deadlockDetected) {
      return DEADLOCK.getCode();
    }

    boolean foreignKeyConstraintViolated = ExceptionUtil.checkForeignKeyConstraintViolation(sqlException, false);
    if (foreignKeyConstraintViolated) {
      return FOREIGN_KEY_CONSTRAINT_VIOLATION.getCode();
    }

    boolean columnSizeTooSmall = ExceptionUtil.checkValueTooLongException(sqlException);
    if (columnSizeTooSmall) {
      return COLUMN_SIZE_TOO_SMALL.getCode();
    }

    return null;
  }

}
